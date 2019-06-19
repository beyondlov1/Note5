package com.beyond.note5.sync.synchronizer;

import com.beyond.note5.bean.Element;
import com.beyond.note5.bean.Tracable;
import com.beyond.note5.sync.Synchronizer;
import com.beyond.note5.sync.datasource.DataSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class SynchronizerSupport<T extends Tracable> implements Synchronizer<T> {
    
    protected DataSource<T> local;
    
    protected DataSource<T> remote;

    @Override
    public void setLocalDataSource(DataSource<T> local) {
        this.local = local;
    }

    @Override
    public void setRemoteDataSource(DataSource<T> remote) {
        this.remote = remote;
    }

    protected abstract Date getLastSyncTime(T t);

    protected List<T> getMergedData(List<T> localData, List<T> remoteData) {

        if (remoteData.isEmpty()){
            return localData;
        }

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

        //获取动过的id
        List<String> deletedDocumentIds = new ArrayList<>();
        List<String> modifyDocumentIds = new ArrayList<>();
        List<String> addDocumentIds = new ArrayList<>();


        Set<String> modifyIdsSet = new HashSet<>();//去重
        for (T localDatum : localData) {

            // 添加本地新增和修改的
            if (localDatum != null && localDatum.getLastModifyTime().compareTo(getLastSyncTime(localDatum)) > 0) {
                modifyIdsSet.add(localDatum.getId());
            }

        }

        // 添加本地删除的
        for (String remoteId : remoteIds) {
            if (!localIds.contains(remoteId)) {
                deletedDocumentIds.add(remoteId);
            }
        }

        for (String modifyId : modifyIdsSet) {
            if (remoteIds.contains(modifyId)) {
                modifyDocumentIds.add(modifyId);
            } else {
                addDocumentIds.add(modifyId);
            }
        }


        //以远程的为主
        List<T> result = new ArrayList<>();
        for (T remoteDatum : remoteData) {
            if (deletedDocumentIds.contains(remoteDatum.getId())) {//删除的不添加
                continue;
            }
            if (modifyDocumentIds.contains(remoteDatum.getId())) {//本地更新的
                T localDatum = getById(localData, remoteDatum.getId());
                if (localDatum != null && localDatum.getLastModifyTime().compareTo(remoteDatum.getLastModifyTime()) < 0) {
                    if (localDatum.getVersion() < remoteDatum.getVersion()) {
                        result.add(remoteDatum);
                    } else {
                        result.add(localDatum);
                    }
                } else {
                    result.add(localDatum);
                }
                continue;
            }
            result.add(remoteDatum);
        }

        for (String addDocumentId : addDocumentIds) {//本地添加的
            result.add(getById(localData, addDocumentId));
        }
        return result;
    }

    protected List<T> getLocalAddedData(List<T> localData, List<T> remoteData){
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

    protected List<T> getRemoteAddedData(List<T> localData, List<T> remoteData){

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

    protected List<T> getLocalUpdatedData(List<T> localData, List<T> remoteData){
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

    protected List<T> getRemoteUpdatedData(List<T> localData, List<T> remoteData){
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

    protected List<T> getLocalDeletedData(List<T> localData, List<T> remoteData){
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

    protected List<T> getRemoteDeleteData(List<T> localData, List<T> remoteData){
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

    protected Long getLatestLastModifyTime(List<T> localData, List<T> remoteData) {
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
        return latestTime.getTime();
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

}
