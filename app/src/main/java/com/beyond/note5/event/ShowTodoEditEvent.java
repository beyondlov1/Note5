package com.beyond.note5.event;

import android.view.View;

public class ShowTodoEditEvent extends AbstractEvent<View>{
    public ShowTodoEditEvent(View view) {
        super(view);
    }
}
