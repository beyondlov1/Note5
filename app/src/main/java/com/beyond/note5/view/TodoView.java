package com.beyond.note5.view;

import com.beyond.note5.bean.Todo;

public interface TodoView extends DocumentView<Todo>{
    void onDeleteReminderSuccess(Todo todo);

    void onDeleteReminderFail(Todo todo);
}
