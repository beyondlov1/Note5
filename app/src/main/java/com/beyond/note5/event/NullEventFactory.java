package com.beyond.note5.event;

public class NullEventFactory {

    private NullEventFactory(){

    }

    public static NullEvent getInstance(){
        return NullEventHolder.nullEvent;
    }

    private static class NullEventHolder{
        static final NullEvent nullEvent = new NullEvent();
    }
}
