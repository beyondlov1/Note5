package com.beyond.note5.sync.synchronizer;

import com.beyond.note5.bean.Tracable;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.webdav.Lock;

import java.util.ArrayList;
import java.util.List;

public abstract class SingleSynchronizerBase<T extends Tracable> extends SynchronizerSupport<T> {

    private ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

    public synchronized boolean sync(DataSource<T> local, DataSource<T> remote) throws Exception {

        Lock remoteLock = getRemoteLock(remote);
        if (remoteLock.tryLock()) {
            List<T> localList = local.selectAll();
            List<T> localData = localList == null ? new ArrayList<>() : localList;
            List<T> remoteList = remote.selectAll();
            List<T> remoteData = remoteList == null ? new ArrayList<>() : remoteList;

            List<T> mergedData = getMergedData(localData, remoteData);

            remote.cover(mergedData);

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
            remoteLock.release();
            return true;
        }

        checkFailCount();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sync(local, remote);

        return true;
    }

    protected abstract void saveLastSyncTime(Long time);

    protected abstract Lock getRemoteLock(DataSource<T> remote);

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
