package com.beyond.note5.sync.model;

import com.beyond.note5.sync.model.bean.SyncLogInfo;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface LogSqlModel {

    void saveAll(List<SyncLogInfo> t) throws IOException;

    List<SyncLogInfo> getAll() throws IOException;

    List<SyncLogInfo> getLocalAdded(Date lastSyncTime);

    List<SyncLogInfo> getLocalUpdated(Date lastSyncTime);

    List<SyncLogInfo> getRemoteAdded(Date lastSyncTime);

    List<SyncLogInfo> getRemoteUpdated(Date lastSyncTime);

    Date getLatestLastModifyTime();
}
