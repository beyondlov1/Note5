package com.beyond.note5.sync.synchronizer;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Tracable;
import com.beyond.note5.sync.Synchronizer;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.DavDataSource;
import com.beyond.note5.sync.model.SqlLogModel;
import com.beyond.note5.sync.model.bean.SyncLogInfo;
import com.beyond.note5.sync.model.bean.TraceInfo;
import com.beyond.note5.sync.model.impl.SqlLogModelImpl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * 在远端保存lastModifyTime的文件， 通过log得到本地新增和减少， 通过remote改变的文件与本地所有文件对比， 得到added和updated的文档
 *
 * @param <T>
 */
public class DavSynchronizer2<T extends Tracable> implements Synchronizer<T> {

    private ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

    private DataSource<T> local;

    private DataSource<T> remote;

    private SqlLogModel localSqlLogModel;

    private DavSynchronizer2() {

    }

    @Override
    public synchronized boolean sync() throws Exception {
        List<T> localList = local.selectAll();
        List<T> localData = localList == null ? new ArrayList<>() : localList;

        TraceInfo remoteTraceInfo = this.remote.getTraceInfo(local);
        TraceInfo localTraceInfo = this.local.getTraceInfo(remote);

        Date remoteLastModifyTime = remoteTraceInfo.getLastModifyTime();
        Date localLastModifyTime = localTraceInfo.getLastModifyTime();

        List<SyncLogInfo> localAdded = localSqlLogModel.getLocalAdded(localLastModifyTime);
        List<SyncLogInfo> localUpdated = localSqlLogModel.getLocalUpdated(localLastModifyTime);

        // 如果有相同的documentId（比如先添加后更新），则只增加
        subtract(localUpdated, localAdded);

        List<T> localAddedData = getLocalData(localAdded);
        List<T> localUpdatedData = getLocalData(localUpdated);

        if (DateUtils.isSameInstant(remoteLastModifyTime, new Date(0))) {
            local.setTraceInfo(TraceInfo.ZERO,remote);
            return syncBaseOnLocal(localAddedData,localUpdatedData);
        }

        if (DateUtils.isSameInstant(localLastModifyTime, remoteLastModifyTime)) {
            return syncBaseOnLocal(localAddedData,localUpdatedData);
        }

        if (remote.tryLock(60000L)) {

            List<String> remoteModifiedIds = new ArrayList<>();
            List<T> remoteModified = remote.selectByModifiedDate(remoteTraceInfo.getLastSyncTime());
            if (remoteModified != null) {
                for (T t : remoteModified) {
                    remoteModifiedIds.add(t.getId());
                }
            }
            List<String> localAllIds = new ArrayList<>();
            if (localList != null) {
                for (T t : localList) {
                    localAllIds.add(t.getId());
                }
            }
            List<T> remoteAddedData = getRemoteAddedData(remoteModifiedIds, localAllIds);
            List<T> remoteUpdatedData = getRemoteUpdatedData(remoteModifiedIds, localAllIds);


            if (localData.isEmpty()) {
                List<T> remoteList = remote.selectAll();
                List<T> remoteData = remoteList == null ? new ArrayList<>() : remoteList;
                for (T remoteDatum : remoteData) {
                    local.add(remoteDatum);
                }

                saveLastSyncTime(getLatestLastModifyTime(localData, remoteData));
                resetFailCount();
                remote.release();
                return true;
            }

            if (!localAddedData.isEmpty()) {
                for (T datum : localAddedData) {
                    remote.add(datum);
                }
            }
            if (!localUpdatedData.isEmpty()) {
                for (T datum : localUpdatedData) {
                    remote.update(datum);
                }
            }

            if (!remoteAddedData.isEmpty()) {
                for (T datum : remoteAddedData) {
                    local.add(datum);
                }
            }
            if (!remoteUpdatedData.isEmpty()) {
                for (T datum : remoteUpdatedData) {
                    local.update(datum);
                }
            }

            saveLastSyncTime(getLatestLastModifyTime(new ArrayList<T>() {
                {
                    addAll(localAddedData);
                    addAll(localUpdatedData);
                }
            }, remoteModified));
            resetFailCount();
            remote.release();
            return true;
        }

        checkFailCount();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sync();

        return true;
    }

    private boolean syncBaseOnLocal(List<T> localAddedData, List<T> localUpdatedData) throws IOException {

        if (localAddedData.isEmpty() && localUpdatedData.isEmpty()) {
            return false;
        }
        if (remote.tryLock(60000L)) {

            if (!localAddedData.isEmpty()) {
                for (T datum : localAddedData) {
                    remote.add(datum);
                }
            }
            if (!localUpdatedData.isEmpty()) {
                for (T datum : localUpdatedData) {
                    remote.update(datum);
                }
            }

            saveLastSyncTime(getLatestLastModifyTime(localAddedData,localUpdatedData));
            resetFailCount();
            remote.release();
            return true;
        }

        checkFailCount();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return syncBaseOnLocal(localAddedData,localUpdatedData);
    }

    private void subtract(List<SyncLogInfo> localUpdated, List<SyncLogInfo> localAdded) {
        Iterator<SyncLogInfo> localIterator = localUpdated.listIterator();
        while (localIterator.hasNext()) {
            SyncLogInfo next = localIterator.next();
            for (SyncLogInfo syncLogInfo : localAdded) {
                if (StringUtils.equals(syncLogInfo.getDocumentId(), next.getDocumentId())) {
                    localIterator.remove();
                }
            }
        }
    }

    private List<T> getRemoteAddedData(List<String> remoteModifiedIds, List<String> localAllIds) throws IOException {
        List<T> result = new ArrayList<>();
        List<String> remoteAddedIds = new ArrayList<>();
        for (String remoteModifiedId : remoteModifiedIds) {
            if (!localAllIds.contains(remoteModifiedId)) {
                remoteAddedIds.add(remoteModifiedId);
            }
        }
        for (String remoteAddedId : remoteAddedIds) {
            result.add(remote.selectById(remoteAddedId));
        }
        return result;
    }

    private List<T> getRemoteUpdatedData(List<String> remoteModifiedIds, List<String> localAllIds) throws IOException {
        List<T> result = new ArrayList<>();
        List<String> remoteAddedIds = new ArrayList<>();
        for (String remoteModifiedId : remoteModifiedIds) {
            if (localAllIds.contains(remoteModifiedId)) {
                remoteAddedIds.add(remoteModifiedId);
            }
        }
        for (String remoteAddedId : remoteAddedIds) {
            result.add(remote.selectById(remoteAddedId));
        }
        return result;
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

    private List<T> getLocalData(List<SyncLogInfo> logs) throws IOException {
        List<String> ids = new ArrayList<>(logs.size());
        for (SyncLogInfo log : logs) {
            ids.add(log.getDocumentId());
        }
        return local.selectByIds(ids);
    }

    @Deprecated
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

    @Deprecated
    private List<T> getLocalAddedData(List<T> localData, Date lastSyncTime) {

        List<T> result = new ArrayList<>();

        for (T localDatum : localData) {
            if (localDatum.getLastModifyTime().after(lastSyncTime)
                    && DateUtils.isSameInstant(localDatum.getCreateTime(), localDatum.getLastModifyTime())) {
                result.add(localDatum);
            }
        }

        return result;
    }

    @Deprecated
    private List<T> getLocalUpdatedData(List<T> localData, Date lastSyncTime) {

        List<T> result = new ArrayList<>();

        for (T localDatum : localData) {
            if (localDatum.getLastModifyTime().after(lastSyncTime)
                    && !DateUtils.isSameInstant(localDatum.getCreateTime(), localDatum.getLastModifyTime())) {
                result.add(localDatum);
            }
        }

        return result;
    }

    private void saveLastSyncTime(Date date) throws IOException {
        TraceInfo traceInfo = TraceInfo.create(date, new Date());
        local.setTraceInfo(traceInfo,remote);
        remote.setTraceInfo(traceInfo,local);
    }

    private void checkFailCount() {
        Integer integer = threadLocal.get();
        if (integer == null) {
            threadLocal.set(0);
        }
        if (threadLocal.get() > 10) {
            throw new RuntimeException("同步失败超过3次");
        }

        threadLocal.set(threadLocal.get() + 1);
    }

    private void resetFailCount() {
        threadLocal.set(0);
    }

    public static class Builder<T extends Tracable> {

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

        public Builder<T> logPath(String logPath) {
            this.logPath = logPath;
            return this;
        }

        public DavSynchronizer2<T> build() {
            DavSynchronizer2<T> synchronizer = new DavSynchronizer2<>();
            if (local == null || remote == null) {
                throw new RuntimeException("local and remote can not be null");
            }
            synchronizer.local = local;
            synchronizer.remote = remote;
//            synchronizer.localSharedTraceInfo = new SqlSharedTraceInfo(local, remote, MyApplication.getInstance().getDaoSession().getSyncInfoDao());
            synchronizer.localSqlLogModel = new SqlLogModelImpl(MyApplication.getInstance().getDaoSession().getSyncLogInfoDao(),
                    local.clazz().getSimpleName().toLowerCase());
            return synchronizer;
        }
    }

}
