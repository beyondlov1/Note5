package com.beyond.note5.event;

public class HideTodoEditorEvent extends AbstractEvent<Integer> {

    public HideTodoEditorEvent(Integer index) {
        super(index);
    }
}
