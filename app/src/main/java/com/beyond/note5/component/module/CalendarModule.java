package com.beyond.note5.component.module;

import android.app.Activity;

import com.beyond.note5.presenter.CalendarPresenter;
import com.beyond.note5.presenter.CalendarPresenterImpl;
import com.beyond.note5.view.CalendarView;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author: beyond
 * @date: 2019/8/27
 */

@Module
public class CalendarModule {

    private CalendarView calendarView;
    private Activity activity;

    public CalendarModule(Activity activity, CalendarView calendarView) {
        this.calendarView = calendarView;
        this.activity = activity;
    }

    @Singleton
    @Provides
    CalendarPresenter provideCalendarPresenter() {
        return new CalendarPresenterImpl(activity, calendarView);
    }
}
