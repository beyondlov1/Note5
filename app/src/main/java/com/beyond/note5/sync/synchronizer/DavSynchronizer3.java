package com.beyond.note5.sync.synchronizer;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Tracable;
import com.beyond.note5.sync.Synchronizer;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.DavDataSource;
import com.beyond.note5.sync.model.SqlLogModel;
import com.beyond.note5.sync.model.SharedSource;
import com.beyond.note5.sync.model.bean.SyncLogInfo;
import com.beyond.note5.sync.model.bean.TraceInfo;
import com.beyond.note5.sync.model.impl.SqlLogModelImpl;
import com.beyond.note5.sync.model.impl.SqlSharedTraceInfo;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * 利用webdav的propfind获取所有file的modifiedDate， 从而与本地的lastSyncTime进行比较
 * @param <T>
 */
public class DavSynchronizer3<T extends Tracable> implements Synchronizer<T> {

    private ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

    private DataSource<T> local;

    private DataSource<T> remote;

    private SharedSource<TraceInfo> localSharedTraceInfo;

    private SqlLogModel localSqlLogModel;

    private DavSynchronizer3() {

    }

    @Override
    public synchronized boolean sync() throws Exception {
        List<T> localList = local.selectAll();
        List<T> localData = localList == null ? new ArrayList<>() : localList;

        Date remoteLastModifyTime = this.remote.getTraceInfo(local).getLastModifyTime();
        Date localLastModifyTime = this.localSharedTraceInfo.get().getLastModifyTime();

        if (DateUtils.isSameInstant(remoteLastModifyTime, new Date(0))) {
            local.setTraceInfo(TraceInfo.ZERO,remote);
            return syncBaseOnLocal(localData);
        }

        if (DateUtils.isSameInstant(localLastModifyTime, remoteLastModifyTime)) {
            return syncBaseOnLocal(localData);
        }

        if (remote.tryLock(60000L)) {

            Date lastModifyTime = localSharedTraceInfo.get().getLastModifyTime();
            List<SyncLogInfo> localAdded = localSqlLogModel.getLocalAdded(lastModifyTime);
            List<SyncLogInfo> localUpdated = localSqlLogModel.getLocalUpdated(lastModifyTime);
            // 如果有相同的documentId（比如先添加后更新），则只增加
            Iterator<SyncLogInfo> localIterator = localUpdated.listIterator();
            while (localIterator.hasNext()) {
                SyncLogInfo next = localIterator.next();
                for (SyncLogInfo syncLogInfo : localAdded) {
                    if (StringUtils.equals(syncLogInfo.getDocumentId(), next.getDocumentId())) {
                        localIterator.remove();
                    }
                }
            }
            List<T> localAddedData = getLocalData(localAdded);
            List<T> localUpdatedData = getLocalData(localUpdated);


            List<String> remoteModifiedIds = new ArrayList<>();
            List<T> remoteModified = remote.selectByModifiedDate(localSharedTraceInfo.get().getLastSyncTime());
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
        List<T> result = new ArrayList<>();
        for (SyncLogInfo syncLogInfo : logs) {
            T t = local.selectById(syncLogInfo.getDocumentId());
            result.add(t);
        }
        return result;
    }

    private boolean syncBaseOnLocal(List<T> localData) throws Exception {
        Date lastModifyTime = this.localSharedTraceInfo.get().getLastModifyTime();
        List<T> localAddedData = getLocalAddedData(localData, lastModifyTime);
        List<T> localUpdatedData = getLocalUpdatedData(localData, lastModifyTime);

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

            saveLastSyncTime(getLatestLastModifyTime(localData));
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

        return syncBaseOnLocal(localData);
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

        public DavSynchronizer3<T> build() {
            DavSynchronizer3<T> synchronizer = new DavSynchronizer3<>();
            if (local == null || remote == null) {
                throw new RuntimeException("local and remote can not be null");
            }
            synchronizer.local = local;
            synchronizer.remote = remote;
            synchronizer.localSharedTraceInfo = new SqlSharedTraceInfo(local, remote, MyApplication.getInstance().getDaoSession().getSyncInfoDao());
            synchronizer.localSqlLogModel = new SqlLogModelImpl(MyApplication.getInstance().getDaoSession().getSyncLogInfoDao(),
                    local.clazz().getSimpleName().toLowerCase());
            return synchronizer;
        }
    }

}
