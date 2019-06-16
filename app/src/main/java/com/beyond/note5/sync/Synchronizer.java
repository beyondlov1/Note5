package com.beyond.note5.sync;

import com.beyond.note5.sync.datasource.DataSource;

public interface Synchronizer<T> {
    boolean sync(DataSource<T> local,DataSource<T> remote) throws Exception;
}
