package com.beyond.note5.event;

import com.beyond.note5.bean.Todo;

/**
 * @author beyondlov1
 * @date 2019/03/25
 */
public class DeleteReminderEvent extends AbstractEvent<Todo>{
    public DeleteReminderEvent(Todo todo) {
        super(todo);
    }
}
