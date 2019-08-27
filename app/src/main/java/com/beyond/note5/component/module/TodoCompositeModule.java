package com.beyond.note5.component.module;

import com.beyond.note5.presenter.CalendarPresenter;
import com.beyond.note5.presenter.PredictPresenter;
import com.beyond.note5.presenter.TodoCompositePresenter;
import com.beyond.note5.presenter.TodoCompositePresenterImpl;
import com.beyond.note5.presenter.TodoPresenter;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author: beyond
 * @date: 2019/8/27
 */
@Module
public class TodoCompositeModule {

    @Singleton
    @Provides
    @Inject
    TodoCompositePresenter todoCompositePresenter(TodoPresenter todoPresenter,
                                                  CalendarPresenter calendarPresenter,
                                                  PredictPresenter predictPresenter){
        return new TodoCompositePresenterImpl.Builder(todoPresenter)
                .calendarPresenter(calendarPresenter)
                .predictPresenter(predictPresenter)
                .build();
    }
}
