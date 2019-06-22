package com.beyond.note5.sync.synchronizer2;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Element;
import com.beyond.note5.bean.Tracable;
import com.beyond.note5.model.dao.SyncInfoDao;
import com.beyond.note5.sync.model.LSTModel;
import com.beyond.note5.sync.model.impl.SqlLSTModel;
import com.beyond.note5.sync.Synchronizer;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.DavDataSource;

import org.apache.commons.lang3.time.DateUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Deprecated
public class DavSynchronizer<T extends Tracable> implements Synchronizer<T> {

    private ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

    private DataSource<T> local;

    private DavDataSource<T> remote;

    private LSTModel localLSTModel;

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

    private boolean syncBaseOnLocal(List<T> localData) throws IOException {
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

    private void saveLastSyncTime(Date date) throws IOException {
        localLSTModel.setLastSyncTime(date);
        remote.setLastSyncTime(date);
    }

    public static class Builder<T extends Tracable>{

        private DataSource<T> local;

        private DavDataSource<T> remote;

        private SyncInfoDao syncInfoDao;

        public Builder() {
        }

        public Builder<T> syncInfoDao(SyncInfoDao syncInfoDao){
            this.syncInfoDao = syncInfoDao;
            return this;
        }

        public Builder localDataSource(DataSource<T> local) {
            this.local = local;
            return this;
        }

        public Builder remoteDataSource(DataSource<T> remote) {
            if (remote instanceof DavDataSource){
                this.remote = (DavDataSource<T>) remote;
            }else {
                throw new RuntimeException("not supported");
            }
            return this;
        }

        public DavSynchronizer<T> build(){
            DavSynchronizer<T> synchronizer= new DavSynchronizer<>();
            if (local == null || remote == null){
                throw new RuntimeException("local and remote can not be null");
            }
            synchronizer.local = local;
            synchronizer.remote = remote;
            if (syncInfoDao == null){
                syncInfoDao = MyApplication.getInstance().getDaoSession().getSyncInfoDao();
            }
            synchronizer.localLSTModel = new SqlLSTModel(local,remote,syncInfoDao);
            return synchronizer;
        }
    }

    protected List<T> getLocalAddedData(List<T> localData, List<T> remoteData) throws IOException {
        //获取本地和远程的文档
        Collections.reverse(localData);
        Collections.reverse(remoteData);

        //获取远程和本地所有id
        List<String> localIds = new ArrayList<>();
        List<String> remoteIds = new ArrayList<>();
        for (T localDatum : localData) {
            localIds.add(localDatum.getId());
        }
        for (T remoteDatum : remoteData) {
            remoteIds.add(remoteDatum.getId());
        }

        List<String> addDocumentIds = new ArrayList<>();


        Set<String> modifyIdsSet = new HashSet<>();//去重
        for (T localDatum : localData) {

            // 添加本地新增和修改的
            if (localDatum != null && localDatum.getLastModifyTime().compareTo(getLastSyncTime(localDatum)) > 0) {
                modifyIdsSet.add(localDatum.getId());
            }

        }

        for (String modifyId : modifyIdsSet) {
            if (!remoteIds.contains(modifyId)){
                addDocumentIds.add(modifyId);
            }
        }


        //以远程的为主
        List<T> result = new ArrayList<>();
        for (String addDocumentId : addDocumentIds) {//本地添加的
            result.add(getById(localData, addDocumentId));
        }
        return result;
    }

    protected List<T> getRemoteAddedData(List<T> localData, List<T> remoteData) throws IOException {

        if (localData.isEmpty()){
            return remoteData;
        }

        //获取本地和远程的文档
        Collections.reverse(localData);
        Collections.reverse(remoteData);

        //获取远程和本地所有id
        List<String> localIds = new ArrayList<>();
        List<String> remoteIds = new ArrayList<>();
        for (T localDatum : localData) {
            localIds.add(localDatum.getId());
        }
        for (T remoteDatum : remoteData) {
            remoteIds.add(remoteDatum.getId());
        }

        List<String> addDocumentIds = new ArrayList<>();


        Set<String> modifyIdsSet = new HashSet<>();//去重
        for (T remoteDatum : remoteData) {

            // 添加本地新增和修改的
            if (remoteDatum != null && remoteDatum.getLastModifyTime().compareTo(getLastSyncTime(remoteDatum)) > 0) {
                modifyIdsSet.add(remoteDatum.getId());
            }

        }

        for (String modifyId : modifyIdsSet) {
            if (!localIds.contains(modifyId)){
                addDocumentIds.add(modifyId);
            }
        }


        //以远程的为主
        List<T> result = new ArrayList<>();
        for (String addDocumentId : addDocumentIds) {//本地添加的
            result.add(getById(remoteData, addDocumentId));
        }
        return result;
    }

    protected List<T> getLocalUpdatedData(List<T> localData, List<T> remoteData) throws IOException {
        //获取本地和远程的文档
        Collections.reverse(localData);
        Collections.reverse(remoteData);

        //获取远程和本地所有id
        List<String> localIds = new ArrayList<>();
        List<String> remoteIds = new ArrayList<>();
        for (T localDatum : localData) {
            localIds.add(localDatum.getId());
        }
        for (T remoteDatum : remoteData) {
            remoteIds.add(remoteDatum.getId());
        }

        List<String> modifyDocumentIds = new ArrayList<>();

        Set<String> modifyIdsSet = new HashSet<>();//去重
        for (T localDatum : localData) {
            if (localDatum != null && localDatum.getLastModifyTime().compareTo(getLastSyncTime(localDatum)) > 0) {
                modifyIdsSet.add(localDatum.getId());
            }

        }

        for (String modifyId : modifyIdsSet) {
            if (remoteIds.contains(modifyId)) {
                modifyDocumentIds.add(modifyId);
            }
        }

        List<T> result = new ArrayList<>();
        // 本地更新的， 并且local和remote不一样的
        for (T remoteDatum : remoteData) {
            if (modifyDocumentIds.contains(remoteDatum.getId())) {//本地更新的
                T localDatum = getById(localData, remoteDatum.getId());
                if (localDatum != null && localDatum.getLastModifyTime().compareTo(remoteDatum.getLastModifyTime()) < 0) {
                    if (localDatum.getVersion() > remoteDatum.getVersion()) {
                        result.add(localDatum);
                    }
                } else if (localDatum != null && localDatum.getLastModifyTime().compareTo(remoteDatum.getLastModifyTime()) > 0){
                    result.add(localDatum);
                }else if (localDatum != null){
                    if (localDatum.getVersion() > remoteDatum.getVersion()) {
                        result.add(localDatum);
                    }
                }
            }
        }
        return result;
    }

    protected List<T> getRemoteUpdatedData(List<T> localData, List<T> remoteData) throws IOException {
        //获取本地和远程的文档
        Collections.reverse(localData);
        Collections.reverse(remoteData);

        //获取远程和本地所有id
        List<String> localIds = new ArrayList<>();
        List<String> remoteIds = new ArrayList<>();
        for (T localDatum : localData) {
            localIds.add(localDatum.getId());
        }
        for (T remoteDatum : remoteData) {
            remoteIds.add(remoteDatum.getId());
        }

        List<String> modifyDocumentIds = new ArrayList<>();

        Set<String> modifyIdsSet = new HashSet<>();//去重
        for (T remoteDatum : remoteData) {

            // 添加本地新增和修改的
            if (remoteDatum != null && remoteDatum.getLastModifyTime().compareTo(getLastSyncTime(remoteDatum)) > 0) {
                modifyIdsSet.add(remoteDatum.getId());
            }

        }

        for (String modifyId : modifyIdsSet) {
            if (remoteIds.contains(modifyId)) {
                modifyDocumentIds.add(modifyId);
            }
        }


        List<T> result = new ArrayList<>();
        for (T remoteDatum : remoteData) {
            if (modifyDocumentIds.contains(remoteDatum.getId())) {//本地更新的
                T localDatum = getById(localData, remoteDatum.getId());
                if (localDatum != null && localDatum.getLastModifyTime().compareTo(remoteDatum.getLastModifyTime()) < 0) {
                    if (localDatum.getVersion() < remoteDatum.getVersion()) {
                        result.add(remoteDatum);
                    }
                }else if (localDatum != null){
                    if (localDatum.getVersion() < remoteDatum.getVersion()) {
                        result.add(remoteDatum);
                    }
                }
            }
        }
        return result;
    }

    protected List<T> getLocalDeletedData(List<T> localData, List<T> remoteData) throws IOException {
        //获取本地和远程的文档
        Collections.reverse(localData);
        Collections.reverse(remoteData);

        //获取远程和本地所有id
        List<String> localIds = new ArrayList<>();
        List<String> remoteIds = new ArrayList<>();
        for (T localDatum : localData) {
            localIds.add(localDatum.getId());
        }
        for (T remoteDatum : remoteData) {
            remoteIds.add(remoteDatum.getId());
        }

        List<String> deletedIds = new ArrayList<>();
        List<String> remoteAddedIds = new ArrayList<>();

        Set<String> modifyIdsSet = new HashSet<>();//去重
        for (T remoteDatum : remoteData) {

            // 添加本地新增和修改的
            if (remoteDatum != null && remoteDatum.getLastModifyTime().compareTo(getLastSyncTime(remoteDatum)) > 0) {
                modifyIdsSet.add(remoteDatum.getId());
            }

        }

        for (String modifyId : modifyIdsSet) {
            if (!localIds.contains(modifyId)){
                remoteAddedIds.add(modifyId);
            }
        }

        // 添加本地删除的
        for (String remoteId : remoteIds) {
            if (!localIds.contains(remoteId)) {
                deletedIds.add(remoteId);
            }
        }


        //以远程的为主
        List<T> result = new ArrayList<>();
        for (String deletedDocumentId : deletedIds) {
            if (!remoteAddedIds.contains(deletedDocumentId)){  // 是remote新增的就不是local要删除的
                result.add(getById(remoteData,deletedDocumentId));
            }
        }
        return result;
    }

    protected List<T> getRemoteDeleteData(List<T> localData, List<T> remoteData) throws IOException {
        if (remoteData.isEmpty()){
            return new ArrayList<>();
        }
        //获取本地和远程的文档
        Collections.reverse(localData);
        Collections.reverse(remoteData);

        //获取远程和本地所有id
        List<String> localIds = new ArrayList<>();
        List<String> remoteIds = new ArrayList<>();
        for (T localDatum : localData) {
            localIds.add(localDatum.getId());
        }
        for (T remoteDatum : remoteData) {
            remoteIds.add(remoteDatum.getId());
        }

        List<String> deletedIds = new ArrayList<>();
        List<String> localAddedIds = new ArrayList<>();


        Set<String> modifyIdsSet = new HashSet<>();//去重
        for (T localDatum : localData) {
            // 添加本地新增和修改的
            if (localDatum != null && localDatum.getLastModifyTime().compareTo(getLastSyncTime(localDatum)) > 0) {
                modifyIdsSet.add(localDatum.getId());
            }

        }

        for (String modifyId : modifyIdsSet) {
            if (!remoteIds.contains(modifyId)){
                localAddedIds.add(modifyId);
            }
        }

        // 添加本地删除的
        for (String localId : localIds) {
            if (!remoteIds.contains(localId)) {
                if (!localAddedIds.contains(localId)){  // 如果是本地增加的则说明不是要删除的
                    deletedIds.add(localId);
                }
            }
        }

        List<T> result = new ArrayList<>();
        for (String deletedDocumentId : deletedIds) {
            result.add(getById(localData,deletedDocumentId));
        }
        return result;
    }

    protected Date getLatestLastModifyTime(List<T> localData, List<T> remoteData) {
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

    private <S extends Element> S getById(List<S> list, String id) {
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(id)) {
                index = i;
            }
        }
        if (index == -1) {
            return null;
        }
        return list.get(index);
    }

    protected Date getLastSyncTime(T t) throws IOException{
        return localLSTModel.getLastSyncTime();
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
