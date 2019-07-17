package com.beyond.note5.sync.datasource.impl;


import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.AbstractEvent;
import com.beyond.note5.event.todo.AddTodoSuccessEvent;
import com.beyond.note5.event.todo.DeleteTodoSuccessEvent;
import com.beyond.note5.presenter.DocumentPresenter;
import com.beyond.note5.presenter.TodoPresenter;
import com.beyond.note5.presenter.TodoPresenterImpl;
import com.beyond.note5.view.adapter.view.TodoViewAdapter;

public class TodoSqlDataSource extends DocumentSqlDataSource<Todo> {


    public TodoSqlDataSource() {
        super();
    }

    public TodoSqlDataSource(TodoPresenter todoPresenter) {
        super(todoPresenter);
    }

    @Override
    protected DocumentPresenter<Todo> getDocumentPresenter() {
        return  new TodoPresenterImpl(new TodoSqlDataSource.MyTodoView());
    }

    @Override
    protected AbstractEvent<Todo> getAddSuccessEvent(Todo todo) {
        return new AddTodoSuccessEvent(todo);
    }

    @Override
    protected AbstractEvent<Todo> getDeleteSuccessEvent(Todo todo) {
        return new DeleteTodoSuccessEvent(todo);
    }

    @Override
    public Class<Todo> clazz() {
        return Todo.class;
    }

    private class MyTodoView extends TodoViewAdapter {
    }
}
