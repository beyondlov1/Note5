package com.beyond.note5.event;

import com.beyond.note5.view.listener.OnKeyboardChangeListener;

public class HideKeyBoardEvent2 extends AbstractEvent<OnKeyboardChangeListener>{
    public HideKeyBoardEvent2(OnKeyboardChangeListener onKeyboardChangeListener) {
        super(onKeyboardChangeListener);
    }

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
