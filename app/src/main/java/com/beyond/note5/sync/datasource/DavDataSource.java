package com.beyond.note5.sync.datasource;

import com.beyond.note5.sync.model.LSTModel;
import com.beyond.note5.sync.webdav.client.DavClient;

public interface DavDataSource<T> extends DataSource<T>, LSTModel {
    String getServer();

    String[] getPaths();

    String getPath(T t);

    DavClient getClient();

}
