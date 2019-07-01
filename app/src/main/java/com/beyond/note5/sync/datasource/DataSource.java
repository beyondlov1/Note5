package com.beyond.note5.sync.datasource;

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

    TraceInfo getTraceInfo(DataSource<T> targetDataSource) throws IOException;
    void setTraceInfo(TraceInfo traceInfo, DataSource<T> targetDataSource) throws IOException;

    Class<T> clazz();
}
