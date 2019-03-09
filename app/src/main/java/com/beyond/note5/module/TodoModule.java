package com.beyond.note5.module;

import android.app.Activity;

import com.beyond.note5.presenter.CalendarPresenter;
import com.beyond.note5.presenter.CalendarPresenterImpl;
import com.beyond.note5.presenter.TodoPresenter;
import com.beyond.note5.presenter.TodoPresenterImpl;
import com.beyond.note5.view.CalendarView;
import com.beyond.note5.view.TodoView;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class TodoModule {
    private Activity activity;
    private TodoView todoView;
    private CalendarView calendarView;

    @Inject
    public TodoModule(Activity activity,TodoView todoView,CalendarView calendarView){
        this.activity = activity;
        this.todoView = todoView;
        this.calendarView  = calendarView;
    }

    @Singleton
    @Provides
    TodoPresenter provideTodoPresenter(){
        return new TodoPresenterImpl(todoView);
    }

    @Singleton
    @Provides
    CalendarPresenter provideCalendarPresenter(){
        return new CalendarPresenterImpl(activity,calendarView);
    }
}
