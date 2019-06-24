package com.beyond.note5.sync.datasource.impl;


import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.presenter.TodoPresenter;
import com.beyond.note5.presenter.TodoPresenterImpl;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.utils.PreferenceUtil;
import com.beyond.note5.view.adapter.view.TodoViewAdapter;

import java.io.IOException;
import java.util.List;

public class TodoSqlDataSource implements DataSource<Todo> {
    
    private TodoPresenter todoPresenter;

    public TodoSqlDataSource() {
        this.todoPresenter = new TodoPresenterImpl(new TodoSqlDataSource.MyTodoView());
    }

    public TodoSqlDataSource(TodoPresenter todoPresenter) {
        this.todoPresenter = todoPresenter;
    }

    @Override
    public String getKey() {
        return PreferenceUtil.getString(MyApplication.VIRTUAL_USER_ID);
    }

    @Override
    public void add(Todo todo) {
        todoPresenter.add(todo);
    }

    @Override
    public void delete(Todo todo) {
        todoPresenter.delete(todo);
    }

    @Override
    public void update(Todo todo) {
        todoPresenter.update(todo);
    }

    @Override
    public Todo select(Todo todo) {
        throw new RuntimeException("暂不支持");
    }

    @Override
    public Todo selectById(String id) throws IOException {
        return todoPresenter.selectById(id);
    }

    @Override
    public List<Todo> selectAll() throws IOException {
        return todoPresenter.selectAllInAll();
    }

    @Override
    public void cover(List<Todo> all) throws IOException {
        throw new RuntimeException("暂不支持");
    }

    @Override
    public Class<Todo> clazz() {
        return Todo.class;
    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(Long time) {
        return false;
    }

    @Override
    public boolean isLocked() {
        return false;
    }

    @Override
    public boolean release() {
        return false;
    }

    private class MyTodoView extends TodoViewAdapter {
    }
}
