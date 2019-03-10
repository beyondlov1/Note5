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

    void onReminderDeleteSuccess(Todo todo);

    void onReminderDeleteFail(Todo todo);

    void onReminderRestoreSuccess(Todo todo);

    void onReminderRestoreFail(Todo todo);
}
