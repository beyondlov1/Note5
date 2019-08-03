package com.beyond.note5.sync.model;

import com.beyond.note5.sync.model.entity.TraceLog;

import java.io.IOException;
import java.util.List;

public interface DavLogModel {
    void saveAll(List<TraceLog> traceLogs) throws IOException;
    List<TraceLog> getAll() throws IOException;
}
