package com.beyond.note5.view;

import com.beyond.note5.bean.Todo;

import java.util.List;

public interface CalendarView {
    void onEventAddSuccess(Todo todo);

    void onEventAddFail(Todo todo);

    void onEventFindAllSuccess(List<Todo> allTodo);

    void onEventFindAllFail();

    void onEventDeleteFail(Todo todo);

    void onEventDeleteSuccess(Todo todo);

    void onEventUpdateFail(Todo todo);

    void onEventUpdateSuccess(Todo todo);

    void onCalendarReminderDeleteSuccess(Todo todo);

    void onCalendarReminderDeleteFail(Todo todo);

    void onCalendarReminderRestoreSuccess(Todo todo);

    void onCalendarReminderRestoreFail(Todo todo);
}
