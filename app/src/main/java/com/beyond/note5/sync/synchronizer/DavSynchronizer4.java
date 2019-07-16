package com.beyond.note5.sync.synchronizer;

import android.util.Log;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Tracable;
import com.beyond.note5.service.SyncRetryService;
import com.beyond.note5.sync.Synchronizer;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.DavDataSource;
import com.beyond.note5.sync.exception.SyncException;
import com.beyond.note5.sync.model.SyncStateModel;
import com.beyond.note5.sync.model.bean.SyncLogInfo;
import com.beyond.note5.sync.model.bean.SyncStateInfo;
import com.beyond.note5.sync.model.bean.TraceInfo;
import com.beyond.note5.sync.model.impl.SyncStateModelImpl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * 基于DavSynchronizer2进行改进， 性能可能下降， 但是更加普适化
 *
 * @param <T>
 */
public class DavSynchronizer4<T extends Tracable> implements Synchronizer<T> {

    private ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

    private DataSource<T> dataSource1;

    private DataSource<T> dataSource2;

    private Date syncStart;

    private SyncStateModel syncStateModel;

    private DavSynchronizer4() {

    }

    @Override
    public synchronized boolean sync() throws Exception {

        Log.d(getClass().getSimpleName(), dataSource2.getKey() + " sync start");

        if (syncStart == null) {
            syncStart = new Date();
        }

        TraceInfo traceInfo1 = this.dataSource1.getCorrespondTraceInfo(dataSource2);
        TraceInfo traceInfo2 = this.dataSource2.getCorrespondTraceInfo(dataSource1);

        Date LastModifyTime1 = traceInfo1.getLastModifyTime();
        Date LastModifyTime2 = traceInfo2.getLastModifyTime();

        if (DateUtils.isSameInstant(LastModifyTime2, new Date(0)) && !DateUtils.isSameInstant(LastModifyTime1, new Date(0))) {
            dataSource1.setLatestTraceInfo(TraceInfo.ZERO);
            dataSource1.setCorrespondTraceInfo(TraceInfo.ZERO, dataSource2);
            List<T> modified1 = dataSource1.getModifiedData(traceInfo1);
            return syncByOneSide(dataSource2, modified1);
        }

        if (DateUtils.isSameInstant(LastModifyTime1, new Date(0)) && !DateUtils.isSameInstant(LastModifyTime2, new Date(0))) {
            dataSource2.setLatestTraceInfo(TraceInfo.ZERO);
            dataSource2.setCorrespondTraceInfo(TraceInfo.ZERO, dataSource1);
            List<T> modified2 = dataSource2.getModifiedData(traceInfo2);
            return syncByOneSide(dataSource1, modified2);
        }

        boolean isDataSource1Changed = dataSource1.isChanged(dataSource2);
        boolean isDataSource2Changed = dataSource2.isChanged(dataSource1);

        if (isDataSource1Changed && !isDataSource2Changed) {
            List<T> modified1 = dataSource1.getModifiedData(traceInfo1);
            return syncByOneSide(dataSource2, modified1);
        }

        if (!isDataSource1Changed && isDataSource2Changed) {
            List<T> modified2 = dataSource2.getModifiedData(traceInfo2);
            return syncByOneSide(dataSource1, modified2);
        }

        if (!isDataSource1Changed && !isDataSource2Changed) {
            return true;
        }

        if (dataSource1.tryLock(60000L) && dataSource2.tryLock(60000L)) {
            List<T> modified1 = dataSource1.getModifiedData(traceInfo1);
            List<T> modified2 = dataSource2.getModifiedData(traceInfo2);

            excludeSuccess(modified1);
            excludeSuccess(modified2);

            try {
                dataSource1.saveAll(modified2);
                recordSyncState(modified2);
            } catch (SyncException e) {
                List<T> successList = modified2.subList(0, e.getFailIndex());
                recordSyncState(successList);
                onFail();
                throw (Exception) e.getCause();
            }

            try {
                dataSource2.saveAll(modified1);
                recordSyncState(modified1);
            } catch (SyncException e) {
                List<T> successList = modified1.subList(0, e.getFailIndex());
                recordSyncState(successList);
                onFail();
                throw (Exception) e.getCause();
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

        sync();

        return true;
    }

    private void onFail() {
        MyApplication.getInstance().handler.post(new Runnable() {
            @Override
            public void run() {
                SyncRetryService.retry(MyApplication.getInstance());
            }
        });
    }

    private boolean syncByOneSide(DataSource<T> changingDataSource, List<T> modified) throws Exception {
        if (modified.isEmpty()) {
            return false;
        }
        if (changingDataSource.tryLock(60000L)) {

            try {
                excludeSuccess(modified);
                changingDataSource.saveAll(modified);
                recordSyncState(modified);
            } catch (SyncException e) {
                List<T> successList = modified.subList(0, e.getFailIndex());
                recordSyncState(successList);
                throw (Exception) e.getCause();
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

    private void clearSyncState() {
        SyncStateInfo syncStateInfo = new SyncStateInfo();
        syncStateInfo.setLocal(dataSource1.getKey());
        syncStateInfo.setServer(dataSource2.getKey());
        syncStateInfo.setType(dataSource1.clazz().getSimpleName().toLowerCase());
        syncStateModel.deleteAll(syncStateInfo);
    }

    private void recordSyncState(List<T> successList) {
        ArrayList<SyncStateInfo> successSyncStates = new ArrayList<>();
        for (T t : successList) {
            SyncStateInfo syncStateInfo = SyncStateInfo.create();
            syncStateInfo.setDocumentId(t.getId());
            syncStateInfo.setLocal(dataSource1.getKey());
            syncStateInfo.setServer(dataSource2.getKey());
            syncStateInfo.setType(dataSource1.clazz().getSimpleName().toLowerCase());
            successSyncStates.add(syncStateInfo);
        }
        syncStateModel.saveAll(successSyncStates);
    }

    private void excludeSuccess(List<T> modified) {
        SyncStateInfo queryState = new SyncStateInfo();
        queryState.setLocal(dataSource1.getKey());
        queryState.setServer(dataSource2.getKey());
        queryState.setState(SyncStateInfo.SUCCESS);
        queryState.setType(dataSource1.clazz().getSimpleName().toLowerCase());
        List<SyncStateInfo> successSynced = syncStateModel.select(queryState);
        List<String> successSyncedIds = new ArrayList<>(successSynced.size());
        for (SyncStateInfo syncStateInfo : successSynced) {
            successSyncedIds.add(syncStateInfo.getDocumentId());
        }
        Iterator<T> iterator = modified.iterator();
        while (iterator.hasNext()) {
            T next = iterator.next();
            if (successSyncedIds.contains(next.getId())) {
                iterator.remove();
            }
        }
    }

    private boolean syncBaseOnLocal(List<T> localAddedData, List<T> localUpdatedData) throws IOException {

        if (localAddedData.isEmpty() && localUpdatedData.isEmpty()) {
            return false;
        }
        if (dataSource2.tryLock(60000L)) {

            if (!localAddedData.isEmpty()) {
                for (T datum : localAddedData) {
                    dataSource2.add(datum);
                }
            }
            if (!localUpdatedData.isEmpty()) {
                for (T datum : localUpdatedData) {
                    dataSource2.update(datum);
                }
            }

            saveLastSyncTime(getLatestLastModifyTime(localAddedData, localUpdatedData));
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

        return syncBaseOnLocal(localAddedData, localUpdatedData);
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
            result.add(dataSource2.selectById(remoteAddedId));
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
            T t = dataSource2.selectById(remoteAddedId);
            if ((t.getLastModifyTime() == null ? new Date(0) : t.getLastModifyTime())
                    .after(dataSource1.selectById(remoteAddedId).getLastModifyTime())) {
                result.add(t);
            }
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
        return dataSource1.selectByIds(ids);
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
        TraceInfo traceInfo = TraceInfo.create(date, syncStart, new Date());
        dataSource1.setCorrespondTraceInfo(traceInfo, dataSource2);
        dataSource2.setCorrespondTraceInfo(traceInfo, dataSource1);
        dataSource1.setLatestTraceInfo(traceInfo);
        dataSource2.setLatestTraceInfo(traceInfo);
        syncStart = null;
    }

    private void checkFailCount() {
        Integer integer = threadLocal.get();
        if (integer == null) {
            threadLocal.set(0);
        }
        if (threadLocal.get() > 10) {
            syncStart = null;
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

        public DavSynchronizer4<T> build() {
            DavSynchronizer4<T> synchronizer = new DavSynchronizer4<>();
            if (local == null || remote == null) {
                throw new RuntimeException("dataSource1 and dataSource2 can not be null");
            }
            synchronizer.dataSource1 = local;
            synchronizer.dataSource2 = remote;
            synchronizer.syncStateModel = new SyncStateModelImpl();
            return synchronizer;
        }
    }

}
