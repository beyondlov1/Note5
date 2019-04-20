package com.beyond.note5.event;

import com.beyond.note5.bean.Todo;

public class ScrollToTodoEvent extends AbstractEvent<Todo>{

    public ScrollToTodoEvent(Todo todo) {
        super(todo);
    }

}
