package com.beyond.note5.module;

import com.beyond.note5.presenter.TodoPresenter;
import com.beyond.note5.presenter.TodoPresenterImpl;
import com.beyond.note5.view.TodoView;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class TodoModule {
    private TodoView todoView;

    @Inject
    public TodoModule(TodoView todoView){
        this.todoView = todoView;
    }

    @Singleton
    @Provides
    TodoPresenter provideTodoPresenter(){
        return new TodoPresenterImpl(todoView);
    }
}
