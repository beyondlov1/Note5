package com.beyond.note5.presenter;

import android.app.Activity;

import com.beyond.note5.bean.Todo;
import com.beyond.note5.model.CalendarModel;
import com.beyond.note5.model.CalendarModelImpl;
import com.beyond.note5.view.CalendarView;

import java.util.List;

public class CalendarPresenterImpl implements CalendarPresenter {

    private CalendarView calendarView;
    private CalendarModel calendarModel;

    public CalendarPresenterImpl(Activity activity, CalendarView calendarView) {
        this.calendarView = calendarView;
        this.calendarModel = new CalendarModelImpl(activity);
    }

    @Override
    public void add(Todo todo) {
        try {
            calendarModel.add(todo);
            this.addSuccess(todo);
        } catch (Exception e) {
            e.printStackTrace();
            this.addFail(todo);
        }
    }

    @Override
    public void addSuccess(Todo todo) {
        calendarView.onEventAddSuccess(todo);
    }

    @Override
    public void addFail(Todo todo) {
        calendarView.onEventAddFail(todo);
    }

    @Override
    public void update(Todo todo) {
        try {
            calendarModel.update(todo);
            this.updateSuccess(todo);
        } catch (Exception e) {
            e.printStackTrace();
            this.updateFail(todo);
        }
    }

    @Override
    public void updateSuccess(Todo todo) {
        calendarView.onEventUpdateSuccess(todo);
    }

    @Override
    public void updateFail(Todo todo) {
        calendarView.onEventUpdateFail(todo);
    }

    @Override
    public void delete(Todo todo) {
        try {
            calendarModel.delete(todo);
            this.deleteSuccess(todo);
        } catch (Exception e) {
            e.printStackTrace();
            this.deleteFail(todo);
        }
    }

    @Override
    public void deleteSuccess(Todo todo) {
        calendarView.onEventDeleteSuccess(todo);
    }

    @Override
    public void deleteFail(Todo todo) {
        calendarView.onEventDeleteFail(todo);
    }

    @Override
    public void findAll() {
        try {
            List<Todo> all = calendarModel.findAll();
            this.findAllSuccess(all);
        } catch (Exception e) {
            e.printStackTrace();
            this.findAllFail();
        }
    }

    @Override
    public void findAllSuccess(List<Todo> allDocument) {
        calendarView.onEventFindAllSuccess(allDocument);
    }

    @Override
    public void findAllFail() {
        calendarView.onEventFindAllFail();
    }

    @Override
    public void deleteReminder(Todo todo) {
        try {
            calendarModel.deleteReminder(todo);
            this.deleteReminderSuccess(todo);
        } catch (Exception e) {
            e.printStackTrace();
            this.deleteReminderFail(todo);
        }
    }

    @Override
    public void deleteReminderSuccess(Todo todo) {
        calendarView.onReminderDeleteSuccess(todo);
    }

    @Override
    public void deleteReminderFail(Todo todo) {
        calendarView.onReminderDeleteFail(todo);
    }
}
