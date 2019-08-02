package com.beyond.note5.sync.model;

import com.beyond.note5.sync.model.entity.SyncStateInfo;

import java.util.List;

public interface SyncStateModel {
    void add(SyncStateInfo syncStateInfo);

    void update(SyncStateInfo syncStateInfo);

    void save(SyncStateInfo syncStateInfo);

    void saveAll(List<SyncStateInfo> successSyncStates);

    List<SyncStateInfo> findAll(SyncStateInfo syncStateInfo);

    void deleteAll(SyncStateInfo queryState);
}
