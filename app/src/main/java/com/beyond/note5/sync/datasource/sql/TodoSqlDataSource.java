package com.beyond.note5.sync.datasource.sql;


import com.beyond.note5.bean.Todo;
import com.beyond.note5.presenter.DocumentPresenter;
import com.beyond.note5.presenter.TodoPresenterImpl;
import com.beyond.note5.view.adapter.view.TodoViewAdapter;

public class TodoSqlDataSource extends DocumentSqlDataSource<Todo> {


    public TodoSqlDataSource(String oppositeKey) {
        super(oppositeKey);
    }

    @Override
    protected DocumentPresenter<Todo> getDocumentPresenter() {
        return  new TodoPresenterImpl(new TodoSqlDataSource.MyTodoView());
    }

    @Override
    public Class<Todo> clazz() {
        return Todo.class;
    }

    private class MyTodoView extends TodoViewAdapter {
    }
}
