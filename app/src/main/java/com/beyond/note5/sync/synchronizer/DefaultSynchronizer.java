package com.beyond.note5.sync.synchronizer;

import android.util.Log;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Tracable;
import com.beyond.note5.service.SyncRetryService;
import com.beyond.note5.sync.SyncContext;
import com.beyond.note5.sync.SyncContextAware;
import com.beyond.note5.sync.SyncContextImpl;
import com.beyond.note5.sync.Synchronizer;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.DavDataSource;
import com.beyond.note5.sync.exception.ConnectException;
import com.beyond.note5.sync.exception.MessageException;
import com.beyond.note5.sync.exception.SaveException;
import com.beyond.note5.sync.model.SyncState;
import com.beyond.note5.sync.model.entity.TraceInfo;

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

    private DataSource<T> dataSource1;

    private DataSource<T> dataSource2;

    private SyncContext context;

    private ExceptionHandler exceptionHandler;

    private long remoteLockTimeOutMills = 30 * 60000L; // 30min

    private Lock lock;

    private Date syncStart;

    private DefaultSynchronizer() {
        lock = new ReentrantLock();
    }

    @Override
    public boolean sync() throws Exception {
        if (lock.tryLock()) {
            try {
                Log.d(getClass().getSimpleName(), dataSource2.getKey() + " sync start");
                ((ThreadPoolExecutor)MyApplication.getInstance().getExecutorService()).setCorePoolSize(CPU_COUNT*2+1);
                doSync();
                ((ThreadPoolExecutor)MyApplication.getInstance().getExecutorService()).setCorePoolSize(0);
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

        TraceInfo traceInfo1 = this.dataSource1.getCorrespondTraceInfo(dataSource2);
        TraceInfo traceInfo2 = this.dataSource2.getCorrespondTraceInfo(dataSource1);

        Date LastModifyTime1 = traceInfo1.getLastModifyTime();
        Date LastModifyTime2 = traceInfo2.getLastModifyTime();

        if (DateUtils.isSameInstant(LastModifyTime2, new Date(0)) && !DateUtils.isSameInstant(LastModifyTime1, new Date(0))) {
            Log.d(getClass().getSimpleName(), dataSource2.getKey() + "为空, 根据" + dataSource1.getKey() + "进行同步");
            dataSource1.setLatestTraceInfo(TraceInfo.ZERO);
            dataSource1.setCorrespondTraceInfo(TraceInfo.ZERO, dataSource2);
            traceInfo1 = this.dataSource1.getCorrespondTraceInfo(dataSource2);
            List<T> modified1 = dataSource1.getChangedData(traceInfo1);
            return syncByOneSide(dataSource2, modified1);
        }

        if (DateUtils.isSameInstant(LastModifyTime1, new Date(0)) && !DateUtils.isSameInstant(LastModifyTime2, new Date(0))) {
            Log.d(getClass().getSimpleName(), dataSource1.getKey() + "为空, 根据" + dataSource2.getKey() + "进行同步");
            dataSource2.setLatestTraceInfo(TraceInfo.ZERO);
            dataSource2.setCorrespondTraceInfo(TraceInfo.ZERO, dataSource1);
            traceInfo2 = this.dataSource2.getCorrespondTraceInfo(dataSource1);
            List<T> modified2 = dataSource2.getChangedData(traceInfo2);
            return syncByOneSide(dataSource1, modified2);
        }

        boolean isDataSource1Changed = dataSource1.isChanged(dataSource2);
        boolean isDataSource2Changed = dataSource2.isChanged(dataSource1);

        Log.d(getClass().getSimpleName(), dataSource1.getKey() + "是否修改:" + isDataSource1Changed + "; " + dataSource2.getKey() + "是否修改:" + isDataSource2Changed);

        if (isDataSource1Changed && !isDataSource2Changed) {
            List<T> modified1 = dataSource1.getChangedData(traceInfo1);
            return syncByOneSide(dataSource2, modified1);
        }

        if (!isDataSource1Changed && isDataSource2Changed) {
            List<T> modified2 = dataSource2.getChangedData(traceInfo2);
            return syncByOneSide(dataSource1, modified2);
        }

        if (!isDataSource1Changed && !isDataSource2Changed) {
            Log.d(getClass().getSimpleName(), "都未修改, 无需同步");
            return true;
        }

        if (dataSource1.tryLock(remoteLockTimeOutMills) && dataSource2.tryLock(remoteLockTimeOutMills)) {
            List<T> modified1 = dataSource1.getChangedData(traceInfo1);
            List<T> modified2 = dataSource2.getChangedData(traceInfo2);

            ignoreSuccess(modified1);
            ignoreSuccess(modified2);

            if (modified1.isEmpty() && modified2.isEmpty()) {
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

            saveLastSyncTime(getLatestLastModifyTime(modified1, modified2));
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

    private void onSaveFail( SaveException e) {
        Log.d(getClass().getSimpleName(), "同步失败", e);
        List<String> successList = e.getSuccessIds();
        context.recordSyncState(successList,SyncState.SUCCESS);
        retryIfNecessary(DEFAULT_RETRY_DELAY);
    }

    private boolean syncByOneSide(DataSource<T> changingDataSource, List<T> modified) throws Exception {
        if (modified.isEmpty()) {
            return false;
        }
        if (changingDataSource.tryLock(remoteLockTimeOutMills)) {

            try {
                ignoreSuccess(modified);
                changingDataSource.saveAll(modified);
                recordSyncSuccessState(modified);
            } catch (SaveException e) {
                onSaveFail(e);
                throw e;
            }

            clearSyncState();

            saveLastSyncTime(getLatestLastModifyTime(modified));
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

        return syncByOneSide(changingDataSource, modified);
    }

    private void recordSyncSuccessState(List<T> successList){
        List<String> ids = new ArrayList<>(successList.size());
        for (T t : successList) {
            ids.add(t.getId());
        }
        context.recordSyncState(ids, SyncState.SUCCESS);
    }

    private void clearSyncState() {
        context.clearSyncState();
    }

    private void ignoreSuccess(List<T> modified) {
        List<String> syncExcludeIds = context.findSyncExcludeIds();
        Iterator<T> iterator = modified.iterator();
        while (iterator.hasNext()) {
            T next = iterator.next();
            if (syncExcludeIds.contains(next.getId())) {
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

    private void saveLastSyncTime(Date date) throws IOException {
        TraceInfo traceInfo = TraceInfo.create(date, syncStart, new Date());
        dataSource1.setCorrespondTraceInfo(traceInfo, dataSource2);
        dataSource2.setCorrespondTraceInfo(traceInfo, dataSource1);
        dataSource1.setLatestTraceInfo(traceInfo);
        dataSource2.setLatestTraceInfo(traceInfo);
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
            throw new ConnectException("连接失败",dataSource2.getKey());
        }

        threadLocal.set(threadLocal.get() + 1);
    }

    private void resetFailCount() {
        threadLocal.set(0);
        SyncRetryService.resetRetryFailCount();
    }

    public static class Builder<T extends Tracable> {

        private SyncContextImpl context;
        private DataSource<T> local;
        private DavDataSource<T> remote;
        private String logPath;

        public Builder() {
        }

        public Builder<T> localDataSource(DataSource<T> local) {
            this.local = local;
            return this;
        }

        public Builder<T> remoteDataSource(DataSource<T> remote) {
            if (remote instanceof DavDataSource) {
                this.remote = (DavDataSource<T>) remote;
            } else {
                throw new RuntimeException("not supported");
            }
            return this;
        }

        public Builder<T> context(SyncContextImpl context) {
            this.context = context;
            return this;
        }

        public Builder<T> logPath(String logPath) {
            this.logPath = logPath;
            return this;
        }

        public DefaultSynchronizer<T> build() {
            DefaultSynchronizer<T> synchronizer = new DefaultSynchronizer<>();
            if (local == null || remote == null) {
                throw new RuntimeException("dataSource1 and dataSource2 can not be null");
            }
            if (context == null) {
                context = new SyncContextImpl(local, remote);
            }
            if (local instanceof SyncContextAware) {
                ((SyncContextAware) local).setContext(context);
            }
            if (remote instanceof SyncContextAware) {
                ((SyncContextAware) remote).setContext(context);
            }
            synchronizer.dataSource1 = local;
            synchronizer.dataSource2 = remote;
            synchronizer.context = context;
            return synchronizer;
        }
    }

}
