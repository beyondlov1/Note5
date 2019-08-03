package com.beyond.note5.sync.context;

import com.beyond.note5.sync.context.entity.SyncState;
import com.beyond.note5.sync.context.model.SyncStateEnum;
import com.beyond.note5.sync.datasource.DataSource;

import java.util.List;

/**
 * @author: beyond
 * @date: 2019/8/2
 */

public interface SyncContext {

    String getOppositeKey(String key);

    String getOppositeKey(DataSource dataSource);

    DataSource getOppositeDataSource(DataSource dataSource);

    DataSource getOppositeDataSource(String key);

    void saveSyncState(String id, SyncStateEnum syncStateEnum);

    void saveSyncState(List<String> ids, SyncStateEnum syncStateEnum);

    void clearSyncState();

    List<SyncState> findSyncStates(SyncStateEnum state);

    void setDataSource1(DataSource dataSource1);

    void setDataSource2(DataSource dataSource2);
}
