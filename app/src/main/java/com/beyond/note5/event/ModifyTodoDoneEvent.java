package com.beyond.note5.event;

import com.beyond.note5.bean.Todo;

public class ModifyTodoDoneEvent extends AbstractEvent<Todo>{
    public ModifyTodoDoneEvent(Todo todo) {
        super(todo);
    }
}
