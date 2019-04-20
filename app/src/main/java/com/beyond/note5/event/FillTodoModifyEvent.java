package com.beyond.note5.event;

import com.beyond.note5.bean.Todo;

public class FillTodoModifyEvent extends AbstractEvent<Todo> {

    private int index;

    public FillTodoModifyEvent(Todo todo) {
        super(todo);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
