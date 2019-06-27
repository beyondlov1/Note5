package com.beyond.note5.sync.datasource;

import com.beyond.note5.sync.model.bean.TraceInfo;
import com.beyond.note5.sync.webdav.client.DavClient;

import java.io.IOException;

public interface DavDataSource<T> extends DataSource<T> {
    String getServer();

    String[] getPaths();

    String getPath(T t);

    DavClient getClient();

    TraceInfo getTraceInfo() throws IOException;
    
    void setTraceInfo(TraceInfo traceInfo) throws IOException;
}
