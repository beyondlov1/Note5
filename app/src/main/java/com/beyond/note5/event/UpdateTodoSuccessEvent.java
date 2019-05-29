package com.beyond.note5.event;

import com.beyond.note5.bean.Todo;

public class UpdateTodoSuccessEvent extends AbstractEvent<Todo> {
    public UpdateTodoSuccessEvent(Todo todo) {
        super(todo);
    }
}
