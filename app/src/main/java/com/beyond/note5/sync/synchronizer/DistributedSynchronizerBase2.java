package com.beyond.note5.sync.synchronizer;

import com.beyond.note5.bean.Tracable;

import org.apache.commons.lang3.time.DateUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@Deprecated
public abstract class DistributedSynchronizerBase2<T extends Tracable> extends SynchronizerSupport<T> {

    private ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

    public synchronized boolean sync() throws Exception {

        if (local == null|| remote == null){
            throw new RuntimeException("datasource can not be null");
        }

        if (remote.tryLock(60000L)) {

            List<T> localList = local.selectAll();
            List<T> localData = localList == null ? new ArrayList<>() : localList;

            if (DateUtils.isSameInstant(this.getLocalLastSyncTime(),this.getRemoteLastSyncTime())){
                pushDirectly(localData, this.getLocalLastSyncTime());
                saveLastSyncTime(getLatestLastModifyTime(localData));
                resetFailCount();
                remote.release();
                return true;
            }

            List<T> remoteList = remote.selectAll();
            List<T> remoteData = remoteList == null ? new ArrayList<>() : remoteList;

            if (localData.isEmpty()){
                for (T remoteDatum : remoteData) {
                    local.add(remoteDatum);
                }

                saveLastSyncTime(getLatestLastModifyTime(localData,remoteData));
                resetFailCount();
                remote.release();
                return true;
            }
            if (remoteData.isEmpty()){
                for (T localDatum : localData) {
                    remote.add(localDatum);
                }

                saveLastSyncTime(getLatestLastModifyTime(localData,remoteData));
                resetFailCount();
                remote.release();
                return true;
            }

            List<T> localAddedData = getLocalAddedData(localData, remoteData);
            List<T> localUpdatedData = getLocalUpdatedData(localData, remoteData);
            List<T> localDeletedData = getLocalDeletedData(localData, remoteData);

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
            if (!localDeletedData.isEmpty()) {
                for (T datum : localDeletedData) {
                    remote.delete(datum);
                }
            }

            List<T> remoteAddedData = getRemoteAddedData(localData, remoteData);
            List<T> remoteUpdatedData = getRemoteUpdatedData(localData, remoteData);
            List<T> remoteDeleteData = getRemoteDeleteData(localData, remoteData);

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
            if (!remoteDeleteData.isEmpty()) {
                for (T datum : remoteDeleteData) {
                    local.delete(datum);
                }
            }


            saveLastSyncTime(getLatestLastModifyTime(localData,remoteData));
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

    protected abstract Date getRemoteLastSyncTime() throws IOException;

    protected abstract Date getLocalLastSyncTime() throws IOException;

    private Long getLatestLastModifyTime(List<T> localData) {
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
        return latestTime.getTime();
    }

    private void pushDirectly(List<T> localData, Date lastSyncTime) throws IOException {
        List<T> localAddedData = getLocalAddedData(localData,lastSyncTime);
        List<T> localUpdatedData = getLocalUpdatedData(localData,lastSyncTime);
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

    protected abstract void saveLastSyncTime(Long time) throws IOException;

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
}
