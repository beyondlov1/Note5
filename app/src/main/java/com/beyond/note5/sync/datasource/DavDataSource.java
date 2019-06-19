package com.beyond.note5.sync.datasource;

public interface DavDataSource<T> extends DataSource<T> {
    String[] getNodes();

    String[] getPaths();

    String getNode(T t);

    String getPath(T t);
}
