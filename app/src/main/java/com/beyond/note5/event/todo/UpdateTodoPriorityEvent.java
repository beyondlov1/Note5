package com.beyond.note5.event.todo;

import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.AbstractEvent;

public class UpdateTodoPriorityEvent extends AbstractEvent<Todo> {
    public UpdateTodoPriorityEvent(Todo todo) {
        super(todo);
    }
}
