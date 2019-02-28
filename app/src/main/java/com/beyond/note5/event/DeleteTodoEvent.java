package com.beyond.note5.event;

import com.beyond.note5.bean.Todo;

public class DeleteTodoEvent extends AbstractEvent<Todo>{
    public DeleteTodoEvent(Todo todo) {
        super(todo);
    }
}
