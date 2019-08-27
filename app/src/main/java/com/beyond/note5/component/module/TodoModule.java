package com.beyond.note5.component.module;

import com.beyond.note5.presenter.TodoPresenter;
import com.beyond.note5.presenter.TodoPresenterImpl;
import com.beyond.note5.view.TodoView;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author: beyond
 * @date: 2019/8/27
 */

@Module
public class TodoModule {

    private TodoView todoView;

    public TodoModule(TodoView todoView) {
        this.todoView = todoView;
    }

    @Singleton
    @Provides
    TodoPresenter provideTodoPresenter(){
        return new TodoPresenterImpl(todoView);
    }

}
