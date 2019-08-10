package com.beyond.note5.sync.context;

import com.beyond.note5.sync.context.model.SyncStateEnum;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.context.model.SyncStateModel;
import com.beyond.note5.sync.context.entity.SyncState;
import com.beyond.note5.sync.context.model.SyncStateModelImpl;

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

    public SyncContextImpl() {
        this.syncStateModel = new SyncStateModelImpl();
    }

    public String getOppositeKey(String key) {
        if (StringUtils.equals(key,dataSource1.getKey())){
            return dataSource2.getKey();
        }

        if (StringUtils.equals(key,dataSource2.getKey())){
            return dataSource1.getKey();
        }

        throw new RuntimeException("can not find the opposite key");
    }

    public String getOppositeKey(DataSource dataSource) {
        return getOppositeKey(dataSource.getKey());
    }

    public DataSource getOppositeDataSource(DataSource dataSource) {
        return getOppositeDataSource(dataSource.getKey());
    }

    public DataSource getOppositeDataSource(String key) {
        if (StringUtils.equals(key, dataSource1.getKey())) {
            return dataSource2;
        }
        if (StringUtils.equals(key, dataSource2.getKey())) {
            return dataSource1;
        }
        throw new RuntimeException("can not find target dataSource");
    }

    public void saveSyncState(String id, SyncStateEnum syncStateEnum) {
        SyncState syncStateInfo = SyncState.create();
        syncStateInfo.setDocumentId(id);
        syncStateInfo.setLocal(dataSource1.getKey());
        syncStateInfo.setServer(dataSource2.getKey());
        syncStateInfo.setState(syncStateEnum.getValue());
        syncStateInfo.setType(dataSource1.clazz().getSimpleName().toLowerCase());
        syncStateModel.save(syncStateInfo);
    }

    public void saveSyncState(List<String> ids, SyncStateEnum syncStateEnum) {
        List<SyncState> syncStates = new ArrayList<>();
        for (String id : ids) {
            SyncState syncStateInfo = SyncState.create();
            syncStateInfo.setDocumentId(id);
            syncStateInfo.setLocal(dataSource1.getKey());
            syncStateInfo.setServer(dataSource2.getKey());
            syncStateInfo.setState(syncStateEnum.getValue());
            syncStateInfo.setType(dataSource1.clazz().getSimpleName().toLowerCase());
            syncStates.add(syncStateInfo);
        }
        syncStateModel.saveAll(syncStates);
    }

    public void clearSyncState(){
        SyncState syncState = new SyncState();
        syncState.setLocal(dataSource1.getKey());
        syncState.setServer(dataSource2.getKey());
        syncState.setType(dataSource1.clazz().getSimpleName().toLowerCase());
        syncStateModel.deleteAll(syncState);
    }

    public List<SyncState> findSyncStates(SyncStateEnum state){
        SyncState queryState = new SyncState();
        queryState.setLocal(dataSource1.getKey());
        queryState.setServer(dataSource2.getKey());
        queryState.setState(state.getValue());
        queryState.setType(dataSource1.clazz().getSimpleName().toLowerCase());
        return syncStateModel.findAll(queryState);
    }

    @Override
    public void setDataSource1(DataSource dataSource1) {
        this.dataSource1 = dataSource1;
    }

    @Override
    public void setDataSource2(DataSource dataSource2) {
        this.dataSource2 = dataSource2;
    }

}
