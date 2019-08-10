package com.beyond.note5.sync.context;

import com.beyond.note5.sync.context.entity.SyncState;

/**
 * @author: beyond
 * @date: 2019/8/2
 */

public interface MultiSyncContext {

    void saveSyncState(SyncState syncState);

    void clearSyncState(String key1,String key2);

}
