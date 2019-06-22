package com.beyond.note5.sync.synchronizer;

import com.beyond.note5.MyApplication;
import com.beyond.note5.sync.model.bean.SyncLogInfo;
import com.beyond.note5.bean.Tracable;
import com.beyond.note5.sync.model.impl.LogDavModelImpl;
import com.beyond.note5.sync.model.LogSqlModel;
import com.beyond.note5.sync.model.impl.LogSqlModelImpl;
import com.beyond.note5.sync.model.LSTModel;
import com.beyond.note5.sync.model.impl.SqlLSTModel;
import com.beyond.note5.sync.Synchronizer;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.DavDataSource;
import com.beyond.note5.utils.OkWebDavUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class DavSynchronizer<T extends Tracable> implements Synchronizer<T> {

    private ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

    private DataSource<T> local;

    private DavDataSource<T> remote;

    private LSTModel localLSTModel;

    private LogSqlModel localLogSqlModel;

    private LogDavSynchronizer logSynchronizer;

    private DavSynchronizer(){

    }

    @Override
    public synchronized boolean sync() throws Exception {
        List<T> localList = local.selectAll();
        List<T> localData = localList == null ? new ArrayList<>() : localList;

        if (DateUtils.isSameInstant(this.localLSTModel.getLastSyncTime(),this.remote.getLastSyncTime())){
            return syncBaseOnLocal(localData);
        }

        if (remote.tryLock(60000L)) {

            // 日志辅助功能只有在remote发生变化时才会起作用， syncBaseOnLocal的时候为了快速所以没有触发
            logSynchronizer.sync();

            List<SyncLogInfo> localAdded = localLogSqlModel.getLocalAdded(localLSTModel.getLastSyncTime());
            List<SyncLogInfo> localUpdated = localLogSqlModel.getLocalUpdated(localLSTModel.getLastSyncTime());
            List<SyncLogInfo> remoteAdded = localLogSqlModel.getRemoteAdded(localLSTModel.getLastSyncTime());
            List<SyncLogInfo> remoteUpdated = localLogSqlModel.getRemoteUpdated(localLSTModel.getLastSyncTime());


            // 如果有相同的documentId（比如先添加后更新），则只增加
            Iterator<SyncLogInfo> localIterator = localUpdated.listIterator();
            while (localIterator.hasNext()) {
                SyncLogInfo next = localIterator.next();
                for (SyncLogInfo syncLogInfo : localAdded) {
                    if (StringUtils.equals(syncLogInfo.getDocumentId(),next.getDocumentId())){
                        localIterator.remove();
                    }
                }
            }
            Iterator<SyncLogInfo> remoteIterator = remoteUpdated.listIterator();
            while (remoteIterator.hasNext()) {
                SyncLogInfo next = remoteIterator.next();
                for (SyncLogInfo syncLogInfo : remoteAdded) {
                    if (StringUtils.equals(syncLogInfo.getDocumentId(),next.getDocumentId())){
                        remoteIterator.remove();
                    }
                }
            }

            List<T> localAddedData = getLocalData(localAdded);
            List<T> localUpdatedData = getLocalData(localUpdated);

            List<T> remoteAddedData = getRemoteData(remoteAdded);
            List<T> remoteUpdatedData = getRemoteData(remoteUpdated);

            if (localData.isEmpty()){
                List<T> remoteList = remote.selectAll();
                List<T> remoteData = remoteList == null ? new ArrayList<>() : remoteList;
                for (T remoteDatum : remoteData) {
                    local.add(remoteDatum);
                }

                saveLastSyncTime(getLatestLastModifyTime(localData,remoteData));
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

            saveLastSyncTime(localLogSqlModel.getLatestLastModifyTime());
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
            if (latestTime == null){
                latestTime = localDatum.getLastModifyTime();
                continue;
            }
            if (localDatum.getLastModifyTime().compareTo(latestTime) > 0){
                latestTime = localDatum.getLastModifyTime();
            }
        }

        for (T localDatum : remoteData) {
            if (latestTime == null){
                latestTime = localDatum.getLastModifyTime();
                continue;
            }
            if (localDatum.getLastModifyTime().compareTo(latestTime) > 0){
                latestTime = localDatum.getLastModifyTime();
            }
        }

        if (latestTime == null){
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
        Date lastSyncTime = this.localLSTModel.getLastSyncTime();
        List<T> localAddedData = getLocalAddedData(localData,lastSyncTime);
        List<T> localUpdatedData = getLocalUpdatedData(localData,lastSyncTime);

        if (localAddedData.isEmpty()&&localUpdatedData.isEmpty()){
            return false;
        }
        if (remote.tryLock(60000L)){

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
            if (latestTime == null){
                latestTime = localDatum.getLastModifyTime();
                continue;
            }
            if (localDatum.getLastModifyTime().compareTo(latestTime) > 0){
                latestTime = localDatum.getLastModifyTime();
            }
        }

        if (latestTime == null){
            latestTime = new Date(0);
        }
        return latestTime;
    }

    private List<T> getLocalAddedData(List<T> localData, Date lastSyncTime){

        List<T> result = new ArrayList<>();

        for (T localDatum : localData) {
            if (localDatum.getLastModifyTime().after(lastSyncTime)
                    && DateUtils.isSameInstant(localDatum.getCreateTime(),localDatum.getLastModifyTime())){
                result.add(localDatum);
            }
        }

        return result;
    }

    private List<T> getLocalUpdatedData(List<T> localData, Date lastSyncTime){

        List<T> result = new ArrayList<>();

        for (T localDatum : localData) {
            if (localDatum.getLastModifyTime().after(lastSyncTime)
                    && !DateUtils.isSameInstant(localDatum.getCreateTime(),localDatum.getLastModifyTime())){
                result.add(localDatum);
            }
        }

        return result;
    }

    private void saveLastSyncTime(Date date) throws IOException {
        localLSTModel.setLastSyncTime(date);
        remote.setLastSyncTime(date);
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

    public static class Builder<T extends Tracable>{

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
            if (remote instanceof DavDataSource){
                this.remote = (DavDataSource<T>) remote;
            }else {
                throw new RuntimeException("not supported");
            }
            return this;
        }

        public Builder<T> logPath(String logPath){
            this.logPath = logPath;
            return this;
        }

        public DavSynchronizer<T> build(){
            DavSynchronizer<T> synchronizer= new DavSynchronizer<>();
            if (local == null || remote == null){
                throw new RuntimeException("local and remote can not be null");
            }
            synchronizer.local = local;
            synchronizer.remote = remote;
            synchronizer.localLSTModel = new SqlLSTModel(local,remote,MyApplication.getInstance().getDaoSession().getSyncInfoDao());
            synchronizer.localLogSqlModel = new LogSqlModelImpl(MyApplication.getInstance().getDaoSession().getSyncLogInfoDao());
            synchronizer.logSynchronizer = new LogDavSynchronizer(synchronizer.localLogSqlModel,
                    new LogDavModelImpl(remote.getClient(),OkWebDavUtil.concat(remote.getServer(),logPath==null?MyApplication.LOG_PATH:logPath)));
            return synchronizer;
        }
    }

}
