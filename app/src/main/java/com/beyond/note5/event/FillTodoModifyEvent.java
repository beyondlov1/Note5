package com.beyond.note5.event;

import com.beyond.note5.bean.Todo;

public class FillTodoModifyEvent extends AbstractEvent<Todo> {
    public FillTodoModifyEvent(Todo todo) {
        super(todo);
    }
}
