package com.beyond.note5.sync.datasource;

import android.support.annotation.Nullable;

import com.beyond.note5.sync.datasource.entity.SyncStamp;
import com.beyond.note5.sync.exception.SaveException;
import com.beyond.note5.sync.webdav.Lock;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface DataSource<T> extends Lock {

    void init();

    String getKey();

    void saveAll(List<T> tList, String... oppositeKeys) throws IOException, SaveException;

    List<T> selectAll() throws IOException, ExecutionException, InterruptedException;

    boolean isChanged(DataSource<T> targetDataSource) throws IOException;

    List<T> getChangedData(SyncStamp syncStamp, @Nullable DataSource<T> targetDataSource) throws IOException;

    SyncStamp getLastSyncStamp(DataSource<T> targetDataSource) throws IOException;

    void updateLastSyncStamp(SyncStamp syncStamp, DataSource<T> targetDataSource) throws IOException;

    SyncStamp getLatestSyncStamp() throws IOException;

    void updateLatestSyncStamp(SyncStamp syncStamp) throws IOException;

    Class<T> clazz();

}
