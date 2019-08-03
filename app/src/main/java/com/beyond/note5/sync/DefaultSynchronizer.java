package com.beyond.note5.sync;

import android.util.Log;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Tracable;
import com.beyond.note5.service.SyncRetryService;
import com.beyond.note5.sync.context.SyncContext;
import com.beyond.note5.sync.context.entity.SyncState;
import com.beyond.note5.sync.context.model.SyncStateEnum;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.entity.SyncStamp;
import com.beyond.note5.sync.exception.ConnectException;
import com.beyond.note5.sync.exception.MessageException;
import com.beyond.note5.sync.exception.SaveException;
import com.beyond.note5.sync.exception.handler.ExceptionHandler;

import org.apache.commons.lang3.time.DateUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.beyond.note5.MyApplication.CPU_COUNT;
import static com.beyond.note5.service.SyncRetryService.DEFAULT_RETRY_DELAY;

/**
 * 基于DavSynchronizer2进行改进， 性能可能下降， 但是更加普适化
 *
 * @param <T>
 */
public class DefaultSynchronizer<T extends Tracable> implements Synchronizer<T> {

    private ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

    private long remoteLockTimeOutMills = 30 * 60000L; // 30min

    private Lock lock;

    private Date syncStart;


    private DataSource<T> dataSource1;

    private DataSource<T> dataSource2;

    private SyncContext context;

    private ExceptionHandler exceptionHandler;

    public DefaultSynchronizer(SyncContext context,
                               DataSource<T> dataSource1,
                               DataSource<T> dataSource2,
                               ExceptionHandler exceptionHandler) {
        this.dataSource1 = dataSource1;
        this.dataSource2 = dataSource2;
        this.context = context;
        this.exceptionHandler = exceptionHandler;
        lock = new ReentrantLock();
    }

    @Override
    public boolean sync() throws Exception {
        if (lock.tryLock()) {
            try {
                Log.d(getClass().getSimpleName(), dataSource2.getKey() + " sync start");
                ((ThreadPoolExecutor) MyApplication.getInstance().getExecutorService()).setCorePoolSize(CPU_COUNT * 2 + 1);
                doSync();
                ((ThreadPoolExecutor) MyApplication.getInstance().getExecutorService()).setCorePoolSize(0);
                Log.d(getClass().getSimpleName(), "同步成功");
            } catch (Exception e) {
                long delay = (new Random().nextInt(20) + 35) * 60 * 1000;
                retryIfNecessary(delay);
                Log.e(getClass().getSimpleName(), "同步失败", e);
                e.printStackTrace();
                throw new MessageException(e);
            } finally {
                lock.unlock();
            }
        } else {
            Log.d(getClass().getSimpleName(), "同步正在进行, 本次同步取消");
        }
        return true;
    }

    private void retryIfNecessary(long delay) {
        SyncRetryService.retryIfNecessary(MyApplication.getInstance(), delay);
        SyncRetryService.addFailCount();
    }

    private boolean doSync() throws Exception {
        if (syncStart == null) {
            syncStart = new Date();
        }

        SyncStamp syncStamp1 = this.dataSource1.getLastSyncStamp(dataSource2);
        SyncStamp syncStamp2 = this.dataSource2.getLastSyncStamp(dataSource1);

        Date LastModifyTime1 = syncStamp1.getLastModifyTime();
        Date LastModifyTime2 = syncStamp2.getLastModifyTime();

        if (DateUtils.isSameInstant(LastModifyTime2, new Date(0)) && !DateUtils.isSameInstant(LastModifyTime1, new Date(0))) {
            Log.d(getClass().getSimpleName(), dataSource2.getKey() + "为空, 根据" + dataSource1.getKey() + "进行同步");
            dataSource1.updateLatestSyncStamp(SyncStamp.ZERO);
            dataSource1.updateLastSyncStamp(SyncStamp.ZERO, dataSource2);
            syncStamp1 = this.dataSource1.getLastSyncStamp(dataSource2);
            List<T> modified1 = dataSource1.getChangedData(syncStamp1);
            return syncByOneSide(dataSource2, modified1, syncStamp1, syncStamp2);
        }

        if (DateUtils.isSameInstant(LastModifyTime1, new Date(0)) && !DateUtils.isSameInstant(LastModifyTime2, new Date(0))) {
            Log.d(getClass().getSimpleName(), dataSource1.getKey() + "为空, 根据" + dataSource2.getKey() + "进行同步");
            dataSource2.updateLatestSyncStamp(SyncStamp.ZERO);
            dataSource2.updateLastSyncStamp(SyncStamp.ZERO, dataSource1);
            syncStamp2 = this.dataSource2.getLastSyncStamp(dataSource1);
            List<T> modified2 = dataSource2.getChangedData(syncStamp2);
            return syncByOneSide(dataSource1, modified2, syncStamp1, syncStamp2);
        }

        boolean isDataSource1Changed = dataSource1.isChanged(dataSource2);
        boolean isDataSource2Changed = dataSource2.isChanged(dataSource1);

        Log.d(getClass().getSimpleName(), dataSource1.getKey() + "是否修改:" + isDataSource1Changed + "; " + dataSource2.getKey() + "是否修改:" + isDataSource2Changed);

        if (isDataSource1Changed && !isDataSource2Changed) {
            List<T> modified1 = dataSource1.getChangedData(syncStamp1);
            return syncByOneSide(dataSource2, modified1, syncStamp1, syncStamp2);
        }

        if (!isDataSource1Changed && isDataSource2Changed) {
            List<T> modified2 = dataSource2.getChangedData(syncStamp2);
            return syncByOneSide(dataSource1, modified2, syncStamp1, syncStamp2);
        }

        if (!isDataSource1Changed && !isDataSource2Changed) {
            Log.d(getClass().getSimpleName(), "都未修改, 无需同步");
            return true;
        }

        if (dataSource1.tryLock(remoteLockTimeOutMills) && dataSource2.tryLock(remoteLockTimeOutMills)) {
            List<T> modified1 = dataSource1.getChangedData(syncStamp1);
            List<T> modified2 = dataSource2.getChangedData(syncStamp2);

            ignoreSuccess(modified1);
            ignoreSuccess(modified2);

            if (modified1.isEmpty() && modified2.isEmpty()) {
                syncSyncStamp(syncStamp1, syncStamp2);
                return false;
            }

            try {
                dataSource1.saveAll(modified2);
                recordSyncSuccessState(modified2);
            } catch (SaveException e) {
                onSaveFail(e);
                throw e;
            }

            try {
                dataSource2.saveAll(modified1);
                recordSyncSuccessState(modified1);
            } catch (SaveException e) {
                onSaveFail(e);
                throw e;
            }

            clearSyncState();

            saveSyncStamp(getLatestLastModifyTime(modified1, modified2));
            resetFailCount();
            dataSource2.release();
            return true;
        }

        checkFailCount();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        doSync();

        return true;
    }

    private void syncSyncStamp(SyncStamp syncStamp1, SyncStamp syncStamp2) throws IOException {
        if (syncStamp1.getLastModifyTime().before(syncStamp2.getLastModifyTime())) {
            syncStamp1.setLastModifyTime(syncStamp2.getLastModifyTime());
        }
        if (syncStamp1.getLastSyncTimeStart().before(syncStamp2.getLastSyncTimeStart())) {
            syncStamp1.setLastSyncTimeStart(syncStamp2.getLastSyncTimeStart());
        }
        if (syncStamp1.getLastSyncTimeEnd().before(syncStamp2.getLastSyncTimeEnd())) {
            syncStamp1.setLastSyncTimeEnd(syncStamp2.getLastSyncTimeEnd());
        }
        dataSource1.updateLastSyncStamp(syncStamp1, dataSource2);
        dataSource2.updateLastSyncStamp(syncStamp1, dataSource1);
        dataSource1.updateLatestSyncStamp(syncStamp1);
        dataSource2.updateLatestSyncStamp(syncStamp1);
        syncStart = null;
    }


    private void onSaveFail(SaveException e) {
        Log.d(getClass().getSimpleName(), "同步失败", e);
        List<String> successList = e.getSuccessIds();
        context.saveSyncState(successList, SyncStateEnum.SUCCESS);
        retryIfNecessary(DEFAULT_RETRY_DELAY);
    }

    private boolean syncByOneSide(DataSource<T> changingDataSource, List<T> modified, SyncStamp syncStamp1, SyncStamp syncStamp2) throws Exception {
        if (changingDataSource.tryLock(remoteLockTimeOutMills)) {
            if (modified.isEmpty()) {
                syncSyncStamp(syncStamp1, syncStamp2);
                return false;
            }
            try {
                ignoreSuccess(modified);
                changingDataSource.saveAll(modified);
                recordSyncSuccessState(modified);
            } catch (SaveException e) {
                onSaveFail(e);
                throw e;
            }

            clearSyncState();

            saveSyncStamp(getLatestLastModifyTime(modified));
            resetFailCount();
            changingDataSource.release();
            return true;
        }

        checkFailCount();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return syncByOneSide(changingDataSource, modified, syncStamp1, syncStamp2);
    }

    private void recordSyncSuccessState(List<T> successList) {
        List<String> ids = new ArrayList<>(successList.size());
        for (T t : successList) {
            ids.add(t.getId());
        }
        context.saveSyncState(ids, SyncStateEnum.SUCCESS);
    }

    private void clearSyncState() {
        context.clearSyncState();
    }

    private void ignoreSuccess(List<T> modified) {
        List<SyncState> successSynced = context.findSyncStates(SyncStateEnum.SUCCESS);
        List<String> successSyncedIds = new ArrayList<>(successSynced.size());
        for (SyncState syncState : successSynced) {
            successSyncedIds.add(syncState.getDocumentId());
        }
        Iterator<T> iterator = modified.iterator();
        while (iterator.hasNext()) {
            T next = iterator.next();
            if (successSyncedIds.contains(next.getId())) {
                iterator.remove();
            }
        }
    }

    private Date getLatestLastModifyTime(List<T> localData) {
        Date latestTime = null;
        for (T localDatum : localData) {
            if (latestTime == null) {
                latestTime = localDatum.getLastModifyTime();
                continue;
            }
            if (localDatum.getLastModifyTime().compareTo(latestTime) > 0) {
                latestTime = localDatum.getLastModifyTime();
            }
        }

        if (latestTime == null) {
            latestTime = new Date(0);
        }
        return latestTime;
    }

    private Date getLatestLastModifyTime(List<T> localData, List<T> remoteData) {
        Date latestTime = null;
        for (T localDatum : localData) {
            if (latestTime == null) {
                latestTime = localDatum.getLastModifyTime();
                continue;
            }
            if (localDatum.getLastModifyTime().compareTo(latestTime) > 0) {
                latestTime = localDatum.getLastModifyTime();
            }
        }

        for (T localDatum : remoteData) {
            if (latestTime == null) {
                latestTime = localDatum.getLastModifyTime();
                continue;
            }
            if (localDatum.getLastModifyTime().compareTo(latestTime) > 0) {
                latestTime = localDatum.getLastModifyTime();
            }
        }

        if (latestTime == null) {
            latestTime = new Date(0);
        }
        return latestTime;
    }

    private void saveSyncStamp(Date date) throws IOException {
        SyncStamp syncStamp = SyncStamp.create(date, syncStart, new Date());
        dataSource1.updateLastSyncStamp(syncStamp, dataSource2);
        dataSource2.updateLastSyncStamp(syncStamp, dataSource1);
        dataSource1.updateLatestSyncStamp(syncStamp);
        dataSource2.updateLatestSyncStamp(syncStamp);
        syncStart = null;
    }

    private void checkFailCount() throws ConnectException {
        Integer integer = threadLocal.get();
        if (integer == null) {
            threadLocal.set(0);
        }
        if (threadLocal.get() > 10) {
            syncStart = null;
            long delay = (new Random().nextInt(70) + 10) * 60 * 1000;
            retryIfNecessary(delay);
            Log.i(getClass().getSimpleName(), "同步失败超过3次, " + delay / 60 / 1000 + "分钟后重试");
            throw new ConnectException("连接失败", dataSource2.getKey());
        }

        threadLocal.set(threadLocal.get() + 1);
    }

    private void resetFailCount() {
        threadLocal.set(0);
        SyncRetryService.resetRetryFailCount();
    }

}
