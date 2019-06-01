package com.beyond.note5.event.todo;

import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.AbstractEvent;

/**
 * @author: beyond
 * @date: 2019/2/2
 */

public class AddTodoSuccessEvent extends AbstractEvent<Todo> {
    public AddTodoSuccessEvent(Todo todo) {
        super(todo);
    }
}
