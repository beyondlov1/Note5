package com.beyond.note5.sync.datasource;

import com.beyond.note5.sync.exception.SyncException;
import com.beyond.note5.sync.model.bean.TraceInfo;
import com.beyond.note5.sync.webdav.Lock;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface DataSource<T> extends Lock{
    String getKey();
    void add(T t) throws IOException;
    void delete(T t) throws IOException;
    void update(T t) throws IOException;
    void cover(List<T> all) throws IOException, ExecutionException, InterruptedException;

    T select(T t) throws IOException;
    T selectById(String id) throws IOException;
    List<T> selectByIds(List<String> ids) throws IOException;
    List<T> selectAll() throws IOException, ExecutionException, InterruptedException;

    List<T> selectByModifiedDate(Date date) throws IOException;

    TraceInfo getLatestTraceInfo() throws IOException;
    void setLatestTraceInfo(TraceInfo traceInfo) throws IOException;

    Class<T> clazz();

    /*new start*/

    List<T> getModifiedData(TraceInfo traceInfo) throws IOException;
    void save(T t) throws IOException;
    void saveAll(List<T> tList) throws IOException, SyncException;
    boolean isChanged(DataSource<T> targetDataSource) throws IOException;
    TraceInfo getCorrespondTraceInfo(DataSource<T> targetDataSource) throws IOException;
    void setCorrespondTraceInfo(TraceInfo traceInfo, DataSource<T> targetDataSource) throws IOException;
    void setTargetDataSourceKey(String targetDataSourceKey);

    /*new end*/
}
