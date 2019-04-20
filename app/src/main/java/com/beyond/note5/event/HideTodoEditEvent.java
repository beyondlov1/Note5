package com.beyond.note5.event;

public class HideTodoEditEvent extends AbstractEvent<Integer> {

    public HideTodoEditEvent(Integer index) {
        super(index);
    }
}
