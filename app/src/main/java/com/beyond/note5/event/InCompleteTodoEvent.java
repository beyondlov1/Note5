package com.beyond.note5.event;

import com.beyond.note5.bean.Todo;

/**
 * @author beyondlov1
 * @date 2019/03/10
 */
public class InCompleteTodoEvent extends AbstractEvent<Todo> {
    public InCompleteTodoEvent(Todo todo) {
        super(todo);
    }
}
