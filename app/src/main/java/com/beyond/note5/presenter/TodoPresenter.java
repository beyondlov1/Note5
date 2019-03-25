package com.beyond.note5.presenter;

import com.beyond.note5.bean.Todo;

public interface TodoPresenter extends DocumentPresenter<Todo>{
    void deleteReminder(Todo todo);
    void onDeleteReminderSuccess(Todo todo);
    void onDeleteReminderFail(Todo todo);
}
