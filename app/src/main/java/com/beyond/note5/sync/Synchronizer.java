package com.beyond.note5.sync;

import java.util.List;

public interface Synchronizer<T> {
    boolean sync(DataSource<T> local,List<DataSource<T>> remotes) throws Exception;
    boolean sync(DataSource<T> local,DataSource<T> remote) throws Exception;
}
