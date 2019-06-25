package com.beyond.note5.sync.datasource;

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
    T select(T t) throws IOException;
    T selectById(String id) throws IOException;
    List<T> selectAll() throws IOException, ExecutionException, InterruptedException;
    List<T> selectByModifiedDate(Date date) throws IOException;
    void cover(List<T> all) throws IOException, ExecutionException, InterruptedException;
    Class<T> clazz();
}
