package com.beyond.note5.sync.datasource.sql.model;

import com.beyond.note5.sync.model.entity.TraceLog;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface SqlLogModel {

    void saveAll(List<TraceLog> t) throws IOException;

    List<TraceLog> getAll() throws IOException;

    List<TraceLog> getLocalAdded(Date lastSyncTime);

    List<TraceLog> getLocalUpdated(Date lastSyncTime);

    List<TraceLog> getRemoteAdded(Date lastSyncTime);

    List<TraceLog> getRemoteUpdated(Date lastSyncTime);

    Date getLatestLastModifyTime();

    List<TraceLog> getAllWhereOperationTimeAfter(Date date);

    List<TraceLog> getAllWhereCreateTimeAfter(Date date);

    List<TraceLog> getAllBySourceWhereCreateTimeAfter(Date date, String source);

    List<TraceLog> getAllBySourceWhereOperationTimeAfter(Date date, String source);

    List<TraceLog> getAllWithoutSourceWhereCreateTimeAfter(Date date, String excludeSource);
}
