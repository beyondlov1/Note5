package com.beyond.note5.sync.datasource.dav;

import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.FileStore;
import com.beyond.note5.sync.webdav.client.DavClient;

public interface DavDataSource<T> extends DataSource<T>,FileStore {
    String getServer();

    String[] getPaths();

    String getPath(T t);

    DavClient getClient();

    DavPathStrategy getPathStrategy();
}
