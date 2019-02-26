package com.beyond.note5.event;

import com.beyond.note5.bean.Todo;

public class CompleteTodoEvent extends AbstractEvent<Todo>{
    public CompleteTodoEvent(Todo todo) {
        super(todo);
    }
}
