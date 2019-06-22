package com.beyond.note5.sync.model;

import com.beyond.note5.sync.model.bean.SyncLogInfo;

import java.io.IOException;
import java.util.List;

public interface LogDavModel {
    void saveAll(List<SyncLogInfo> syncLogInfos) throws IOException;
    List<SyncLogInfo> getAll() throws IOException;
}
