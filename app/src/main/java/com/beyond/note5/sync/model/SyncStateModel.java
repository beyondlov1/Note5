package com.beyond.note5.sync.model;

import com.beyond.note5.sync.model.bean.SyncStateInfo;

import java.util.ArrayList;
import java.util.List;

public interface SyncStateModel {
    void add(SyncStateInfo syncStateInfo);
    void update(SyncStateInfo syncStateInfo);
    void saveAll(ArrayList<SyncStateInfo> successSyncStates);

    List<SyncStateInfo> select(SyncStateInfo syncStateInfo);

    void deleteAll(SyncStateInfo queryState);
}
