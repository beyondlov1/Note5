package com.beyond.note5.presenter;

import android.app.Activity;
import android.support.annotation.Nullable;

import com.beyond.note5.bean.Todo;
import com.beyond.note5.model.CalendarModel;
import com.beyond.note5.model.CalendarModelImpl;
import com.beyond.note5.view.CalendarView;

import java.util.List;

public class CalendarPresenterImpl implements CalendarPresenter {

    private CalendarView calendarView;
    private CalendarModel calendarModel;

    public CalendarPresenterImpl(Activity activity, @Nullable CalendarView calendarView) {
        this.calendarView = calendarView;
        this.calendarModel = CalendarModelImpl.getRelativeSingletonInstance(activity);
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
        if (calendarView == null) {
            return;
        }
        calendarView.onEventAddSuccess(todo);
    }

    @Override
    public void addFail(Todo todo) {
        if (calendarView == null) {
            return;
        }
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
        if (calendarView == null) {
            return;
        }
        calendarView.onEventUpdateSuccess(todo);
    }

    @Override
    public void updateFail(Todo todo) {
        if (calendarView == null) {
            return;
        }
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
        if (calendarView == null) {
            return;
        }
        calendarView.onEventDeleteSuccess(todo);
    }

    @Override
    public void deleteFail(Todo todo) {
        if (calendarView == null) {
            return;
        }
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
        if (calendarView == null) {
            return;
        }
        calendarView.onEventFindAllSuccess(allDocument);
    }

    @Override
    public void findAllFail() {
        if (calendarView == null) {
            return;
        }
        calendarView.onEventFindAllFail();
    }

    @Override
    public void deleteReminder(Todo todo) {
        if (todo.getReminder() == null) {
            return;
        }
        try {
            calendarModel.deleteReminder(todo);
            this.deleteCalendarReminderSuccess(todo);
        } catch (Exception e) {
            e.printStackTrace();
            this.deleteCalendarReminderFail(todo);
        }
    }

    @Override
    public void deleteCalendarReminderSuccess(Todo todo) {
        if (calendarView == null) {
            return;
        }
        calendarView.onCalendarReminderDeleteSuccess(todo);
    }

    @Override
    public void deleteCalendarReminderFail(Todo todo) {
        if (calendarView == null) {
            return;
        }
        calendarView.onCalendarReminderDeleteFail(todo);
    }

    @Override
    public void restoreReminder(Todo todo) {
        if (todo.getReminder() == null) {
            return;
        }
        try {
            calendarModel.restoreCalendarReminder(todo);
            this.restoreReminderSuccess(todo);
        } catch (Exception e) {
            e.printStackTrace();
            this.restoreReminderFail(todo);
        }
    }

    @Override
    public void restoreReminderSuccess(Todo todo) {
        if (calendarView == null) {
            return;
        }
        calendarView.onCalendarReminderRestoreSuccess(todo);
    }

    @Override
    public void restoreReminderFail(Todo todo) {
        if (calendarView == null) {
            return;
        }
        calendarView.onCalendarReminderRestoreFail(todo);

    }
}
