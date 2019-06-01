package com.beyond.note5.event.todo;

import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.AbstractEvent;

public class CompleteTodoEvent extends AbstractEvent<Todo> {
    public CompleteTodoEvent(Todo todo) {
        super(todo);
    }
}
