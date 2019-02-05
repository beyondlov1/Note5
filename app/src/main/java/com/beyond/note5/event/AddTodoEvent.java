package com.beyond.note5.event;

import com.beyond.note5.bean.Todo;

/**
 * Created by beyond on 2019/2/2.
 */

public class AddTodoEvent extends AbstractEvent<Todo>{
    public AddTodoEvent(Todo todo) {
        super(todo);
    }
}
