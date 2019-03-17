package com.beyond.note5.event;

import com.beyond.note5.bean.Todo;

public class RefreshTodoListEvent extends AbstractEvent<Todo> {
    private String clickContent;

    public RefreshTodoListEvent(Todo todo) {
        super(todo);
    }

    public RefreshTodoListEvent(Todo todo, String clickContent) {
        super(todo);
        this.clickContent = clickContent;
    }

    public String getClickContent() {
        return clickContent;
    }

    public void setClickContent(String clickContent) {
        this.clickContent = clickContent;
    }
}
