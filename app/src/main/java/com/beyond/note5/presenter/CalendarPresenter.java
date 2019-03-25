package com.beyond.note5.presenter;

import com.beyond.note5.bean.Todo;

public interface CalendarPresenter extends DocumentPresenter<Todo>{
    void deleteReminder(Todo todo);
    void deleteCalendarReminderSuccess(Todo todo);
    void deleteCalendarReminderFail(Todo todo);
    void restoreReminder(Todo todo);
    void restoreReminderSuccess(Todo todo);
    void restoreReminderFail(Todo todo);
}
