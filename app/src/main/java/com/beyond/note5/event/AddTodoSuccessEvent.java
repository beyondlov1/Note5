package com.beyond.note5.event;

import com.beyond.note5.bean.Todo;

/**
 * @author: beyond
 * @date: 2019/2/2
 */

public class AddTodoSuccessEvent extends AbstractEvent<Todo>{
    public AddTodoSuccessEvent(Todo todo) {
        super(todo);
    }
}
