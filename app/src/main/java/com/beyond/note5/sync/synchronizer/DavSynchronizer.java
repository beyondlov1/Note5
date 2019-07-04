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
import com.beyond.note5.sync.model.impl.DavLogModelImpl;
import com.beyond.note5.sync.model.impl.SqlLogModelImpl;
import com.beyond.note5.sync.model.impl.SqlSharedTraceInfo;
import com.beyond.note5.utils.OkWebDavUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * 利用log来确定added， updated， 在本地和远端都保存一份log， 来通知所有人修改了那些文档
 * 但是这个有个问题： 每次本地改变就要维护上传一个log， 如果log文件变得特别大，就会传输很大的文件
 * @param <T>
 */
public class DavSynchronizer<T extends Tracable> implements Synchronizer<T> {

    private ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

    private DataSource<T> local;

    private DavDataSource<T> remote;

    private SharedSource<TraceInfo> localSharedTraceInfo;

    private SqlLogModel localSqlLogModel;

    private LogDavSynchronizer logSynchronizer;

    private DavSynchronizer() {

    }

    @Override
    public synchronized boolean sync() throws Exception {
        List<T> localList = local.selectAll();
        List<T> localData = localList == null ? new ArrayList<>() : localList;

        Date remoteLastModifyTime = this.remote.getCorrespondTraceInfo(local).getLastModifyTime();
        Date localLastModifyTime = this.remote.getCorrespondTraceInfo(remote).getLastModifyTime();

        if (DateUtils.isSameInstant(remoteLastModifyTime,new Date(0))){
            local.setCorrespondTraceInfo(TraceInfo.ZERO,remote);
            return syncBaseOnLocal(localData);
        }

        if (DateUtils.isSameInstant(localLastModifyTime, remoteLastModifyTime)) {
            return syncBaseOnLocal(localData);
        }

        if (remote.tryLock(60000L)) {

            logSynchronizer.sync();

            Date lastModifyTime = local.getCorrespondTraceInfo(remote).getLastModifyTime();
            List<SyncLogInfo> localAdded = localSqlLogModel.getLocalAdded(lastModifyTime);
            List<SyncLogInfo> localUpdated = localSqlLogModel.getLocalUpdated(lastModifyTime);
            List<SyncLogInfo> remoteAdded = localSqlLogModel.getRemoteAdded(lastModifyTime);
            List<SyncLogInfo> remoteUpdated = localSqlLogModel.getRemoteUpdated(lastModifyTime);


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
            Iterator<SyncLogInfo> remoteIterator = remoteUpdated.listIterator();
            while (remoteIterator.hasNext()) {
                SyncLogInfo next = remoteIterator.next();
                for (SyncLogInfo syncLogInfo : remoteAdded) {
                    if (StringUtils.equals(syncLogInfo.getDocumentId(), next.getDocumentId())) {
                        remoteIterator.remove();
                    }
                }
            }

            List<T> localAddedData = getLocalData(localAdded);
            List<T> localUpdatedData = getLocalData(localUpdated);

            List<T> remoteAddedData = getRemoteData(remoteAdded);
            List<T> remoteUpdatedData = getRemoteData(remoteUpdated);

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

            saveLastSyncTime(localSqlLogModel.getLatestLastModifyTime());
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

    private List<T> getRemoteData(List<SyncLogInfo> logs) throws IOException {
        List<T> result = new ArrayList<>();
        for (SyncLogInfo syncLogInfo : logs) {
            T t = remote.selectById(syncLogInfo.getDocumentId());
            result.add(t);
        }
        return result;
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
        Date lastModifyTime = this.local.getCorrespondTraceInfo(remote).getLastModifyTime();
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
            logSynchronizer.sync();  // 没测过
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
        local.setCorrespondTraceInfo(traceInfo,remote);
        remote.setCorrespondTraceInfo(traceInfo,local);
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

        public DavSynchronizer<T> build() {
            DavSynchronizer<T> synchronizer = new DavSynchronizer<>();
            if (local == null || remote == null) {
                throw new RuntimeException("local and remote can not be null");
            }
            synchronizer.local = local;
            synchronizer.remote = remote;
            synchronizer.localSharedTraceInfo = new SqlSharedTraceInfo(local, remote, MyApplication.getInstance().getDaoSession().getSyncInfoDao());
            synchronizer.localSqlLogModel = new SqlLogModelImpl(MyApplication.getInstance().getDaoSession().getSyncLogInfoDao(),
                    local.clazz().getSimpleName().toLowerCase());
            synchronizer.logSynchronizer = new LogDavSynchronizer(synchronizer.localSqlLogModel,
                    new DavLogModelImpl(remote.getClient(), OkWebDavUtil.concat(remote.getServer(), logPath == null ? MyApplication.LOG_PATH : logPath)));
            return synchronizer;
        }
    }

}
