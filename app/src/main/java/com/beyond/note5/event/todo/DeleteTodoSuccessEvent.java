package com.beyond.note5.event.todo;

import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.AbstractEvent;

public class DeleteTodoSuccessEvent extends AbstractEvent<Todo> {
    public DeleteTodoSuccessEvent(Todo todo) {
        super(todo);
    }
}
