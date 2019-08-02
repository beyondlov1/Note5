package com.beyond.note5.sync;

import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.model.SyncState;
import com.beyond.note5.sync.model.SyncStateModel;
import com.beyond.note5.sync.model.entity.SyncStateInfo;
import com.beyond.note5.sync.model.impl.SyncStateModelImpl;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: beyond
 * @date: 2019/7/28
 */

public class SyncContextImpl implements SyncContext {

    private SyncStateModel syncStateModel;

    protected DataSource dataSource1;
    protected DataSource dataSource2;

    public SyncContextImpl(DataSource dataSource1, DataSource dataSource2) {
        this.dataSource1 = dataSource1;
        this.dataSource2 = dataSource2;
        this.syncStateModel = new SyncStateModelImpl();
    }

    public SyncContextImpl(DataSource dataSource1, DataSource dataSource2, SyncStateModel syncStateModel) {
        this.dataSource1 = dataSource1;
        this.dataSource2 = dataSource2;
        this.syncStateModel = syncStateModel;
    }

    public String getCorrespondKey(String key) {
        return getCorrespondDataSource(key).getKey();
    }

    public String getCorrespondKey(DataSource dataSource) {
        return getCorrespondDataSource(dataSource).getKey();
    }

    public DataSource getCorrespondDataSource(DataSource dataSource) {
        return getCorrespondDataSource(dataSource.getKey());
    }

    public DataSource getCorrespondDataSource(String key) {
        if (StringUtils.equals(key, dataSource1.getKey())) {
            return dataSource2;
        }
        if (StringUtils.equals(key, dataSource2.getKey())) {
            return dataSource1;
        }
        throw new RuntimeException("can not find target dataSource");
    }

    public void recordSyncState(String id, SyncState syncState) {
        SyncStateInfo syncStateInfo = SyncStateInfo.create();
        syncStateInfo.setDocumentId(id);
        syncStateInfo.setLocal(dataSource1.getKey());
        syncStateInfo.setServer(dataSource2.getKey());
        syncStateInfo.setState(syncState.getValue());
        syncStateInfo.setType(dataSource1.clazz().getSimpleName().toLowerCase());
        syncStateModel.save(syncStateInfo);
    }

    public void recordSyncState(List<String> ids, SyncState syncState) {
        List<SyncStateInfo> syncStateInfos = new ArrayList<>();
        for (String id : ids) {
            SyncStateInfo syncStateInfo = SyncStateInfo.create();
            syncStateInfo.setDocumentId(id);
            syncStateInfo.setLocal(dataSource1.getKey());
            syncStateInfo.setServer(dataSource2.getKey());
            syncStateInfo.setState(syncState.getValue());
            syncStateInfo.setType(dataSource1.clazz().getSimpleName().toLowerCase());
            syncStateInfos.add(syncStateInfo);
        }
        syncStateModel.saveAll(syncStateInfos);
    }

    public void clearSyncState(){
        SyncStateInfo syncStateInfo = new SyncStateInfo();
        syncStateInfo.setLocal(dataSource1.getKey());
        syncStateInfo.setServer(dataSource2.getKey());
        syncStateInfo.setType(dataSource1.clazz().getSimpleName().toLowerCase());
        syncStateModel.deleteAll(syncStateInfo);
    }

    public List<String> findSyncExcludeIds(){
        SyncStateInfo queryState = new SyncStateInfo();
        queryState.setLocal(dataSource1.getKey());
        queryState.setServer(dataSource2.getKey());
        queryState.setState(SyncStateInfo.SUCCESS);
        queryState.setType(dataSource1.clazz().getSimpleName().toLowerCase());
        List<SyncStateInfo> successSynced = syncStateModel.findAll(queryState);
        List<String> successSyncedIds = new ArrayList<>(successSynced.size());
        for (SyncStateInfo syncStateInfo : successSynced) {
            successSyncedIds.add(syncStateInfo.getDocumentId());
        }
        return successSyncedIds;
    }

}
