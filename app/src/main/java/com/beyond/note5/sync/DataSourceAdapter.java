package com.beyond.note5.sync;

public interface DataSourceAdapter<T> {
    DataSource<T> download();
    boolean upload(DataSource<T> dataSource);
}
