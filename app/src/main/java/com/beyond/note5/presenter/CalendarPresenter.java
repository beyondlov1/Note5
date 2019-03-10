package com.beyond.note5.presenter;

import com.beyond.note5.bean.Todo;

public interface CalendarPresenter extends DocumentPresenter<Todo>{
    void deleteReminder(Todo todo);
    void deleteReminderSuccess(Todo todo);
    void deleteReminderFail(Todo todo);
    void restoreReminder(Todo todo);
    void restoreReminderSuccess(Todo todo);
    void restoreReminderFail(Todo todo);
}