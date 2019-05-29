package com.beyond.note5.event;

import com.beyond.note5.bean.Todo;

public class DeleteTodoSuccessEvent extends AbstractEvent<Todo>{
    public DeleteTodoSuccessEvent(Todo todo) {
        super(todo);
    }
}
