package com.beyond.note5.sync.datasource.impl;


import com.beyond.note5.bean.Todo;
import com.beyond.note5.sync.SyncContext;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.DavDataSource;
import com.beyond.note5.sync.datasource.SqlDataSource;
import com.beyond.note5.sync.exception.SaveException;
import com.beyond.note5.sync.model.bean.TraceInfo;

import java.io.IOException;
import java.util.List;

public class TodoSqlDataSourceWrap implements SqlDataSource<Todo> {

    private final TodoSqlDataSource todoSqlDataSource;

    private DavDataSource<Todo> davDataSource;

    public TodoSqlDataSourceWrap( ) {
        this.todoSqlDataSource = new TodoSqlDataSource();
    }

    public TodoSqlDataSourceWrap(TodoSqlDataSource todoSqlDataSource) {
        this.todoSqlDataSource = todoSqlDataSource;
    }

    @Override
    public Class<Todo> clazz() {
        return todoSqlDataSource.clazz();
    }

    @Override
    public String getKey() {
        return todoSqlDataSource.getKey();
    }

    @Override
    public void add(Todo todo) {
        todoSqlDataSource.add(todo);
    }

    @Override
    public void delete(Todo todo) {
        todoSqlDataSource.delete(todo);
    }

    @Override
    public void update(Todo todo) {
        todoSqlDataSource.update(todo);
    }

    @Override
    public Todo select(Todo todo) {
        return todoSqlDataSource.select(todo);
    }

    @Override
    public Todo selectById(String id) throws IOException {
        return todoSqlDataSource.selectById(id);
    }

    @Override
    public List<Todo> selectByIds(List<String> ids) {
        return todoSqlDataSource.selectByIds(ids);
    }

    @Override
    public List<Todo> selectAll() throws IOException {
        return todoSqlDataSource.selectAll();
    }

    @Override
    public TraceInfo getLatestTraceInfo() throws IOException {
        return todoSqlDataSource.getLatestTraceInfo();
    }

    @Override
    public void setLatestTraceInfo(TraceInfo traceInfo) throws IOException {
        todoSqlDataSource.setLatestTraceInfo(traceInfo);
    }

    @Override
    public void cover(List<Todo> all) throws IOException {
        todoSqlDataSource.cover(all);
    }

    @Override
    public List<Todo> getChangedData(TraceInfo traceInfo) throws IOException {
        return todoSqlDataSource.getChangedData(traceInfo);
    }

    @Override
    public void save(Todo todo) throws IOException {
        todoSqlDataSource.save(todo);
    }

    @Override
    public void saveAll(List<Todo> todos) throws IOException {
        todoSqlDataSource.saveAll(todos,davDataSource.getKey());
    }

    @Override
    public void saveAll(List<Todo> todos, String source) throws IOException, SaveException {
        todoSqlDataSource.saveAll(todos,source);
    }

    @Override
    public boolean isChanged(DataSource<Todo> targetDataSource) throws IOException {
        return todoSqlDataSource.isChanged(targetDataSource);
    }

    @Override
    public TraceInfo getCorrespondTraceInfo(DataSource<Todo> targetDataSource) throws IOException {
        return todoSqlDataSource.getCorrespondTraceInfo(targetDataSource);
    }

    @Override
    public void setCorrespondTraceInfo(TraceInfo traceInfo, DataSource<Todo> targetDataSource) throws IOException {
        todoSqlDataSource.setCorrespondTraceInfo(traceInfo, targetDataSource);
    }

    @Override
    public boolean tryLock() {
        return todoSqlDataSource.tryLock();
    }

    @Override
    public boolean tryLock(Long time) {
        return todoSqlDataSource.tryLock(time);
    }

    @Override
    public boolean isLocked() {
        return todoSqlDataSource.isLocked();
    }

    @Override
    public boolean release() {
        return todoSqlDataSource.release();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setContext(SyncContext context) {
        todoSqlDataSource.setContext(context);
        if (context.getCorrespondDataSource(this) instanceof DavDataSource){
            davDataSource = (DavDataSource<Todo>) context.getCorrespondDataSource(this);
        }
    }
}
