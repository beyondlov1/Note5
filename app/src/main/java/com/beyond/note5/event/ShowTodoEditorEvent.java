package com.beyond.note5.event;

import android.view.View;

public class ShowTodoEditorEvent extends AbstractEvent<View>{
    public ShowTodoEditorEvent(View view) {
        super(view);
    }
}
