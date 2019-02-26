package com.beyond.note5.event;

import com.beyond.note5.bean.Todo;

public class UpdateTodoEvent extends AbstractEvent<Todo> {
    public UpdateTodoEvent(Todo todo) {
        super(todo);
    }
}
