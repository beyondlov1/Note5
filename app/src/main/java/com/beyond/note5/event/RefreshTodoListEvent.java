package com.beyond.note5.event;

import com.beyond.note5.bean.Todo;

public class RefreshTodoListEvent extends AbstractEvent<Todo> {
    public RefreshTodoListEvent(Todo todo) {
        super(todo);
    }
}
