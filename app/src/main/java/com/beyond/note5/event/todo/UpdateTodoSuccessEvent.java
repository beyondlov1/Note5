package com.beyond.note5.event.todo;

import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.AbstractEvent;

public class UpdateTodoSuccessEvent extends AbstractEvent<Todo> {
    public UpdateTodoSuccessEvent(Todo todo) {
        super(todo);
    }
}
