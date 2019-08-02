package com.beyond.note5.sync;

import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.model.SyncState;

import java.util.List;

/**
 * @author: beyond
 * @date: 2019/8/2
 */

public interface SyncContext {
    public String getCorrespondKey(String key);

    public String getCorrespondKey(DataSource dataSource);

    public DataSource getCorrespondDataSource(DataSource dataSource);

    public DataSource getCorrespondDataSource(String key);

    public void recordSyncState(String id, SyncState syncState);

    public void recordSyncState(List<String> ids, SyncState syncState);

    public void clearSyncState();

    public List<String> findSyncExcludeIds();
}
