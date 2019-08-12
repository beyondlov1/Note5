package com.beyond.note5.sync.context.model;

import com.beyond.note5.sync.context.entity.SyncState;

import java.util.List;

public interface SyncStateModel {
    void add(SyncState syncState);

    void update(SyncState syncState);

    void save(SyncState syncState);

    void saveAll(List<SyncState> successSyncStates);

    List<SyncState> findAll(SyncState syncState);

    void deleteAll(SyncState queryState);

    void deleteConnectedEachOtherIn(List<String> keys, Class clazz);
}
