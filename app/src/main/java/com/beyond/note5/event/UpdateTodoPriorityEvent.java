package com.beyond.note5.event;

import com.beyond.note5.bean.Todo;

public class UpdateTodoPriorityEvent extends AbstractEvent<Todo>{
    public UpdateTodoPriorityEvent(Todo todo) {
        super(todo);
    }
}
