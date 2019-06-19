package com.beyond.note5.sync;

import com.beyond.note5.sync.datasource.DataSource;

public interface Synchronizer<T> {
    boolean sync() throws Exception;
    void setLocalDataSource(DataSource<T> local);
    void setRemoteDataSource(DataSource<T> remote);
}
