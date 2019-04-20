package com.beyond.note5.event;

public class NullEvent implements Event {

    @Override
    public Object get() {
        return null;
    }

}
