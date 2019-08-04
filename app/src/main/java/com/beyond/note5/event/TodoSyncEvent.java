package com.beyond.note5.event;

/**
 * @author: beyond
 * @date: 2019/8/4
 */

public class TodoSyncEvent extends AbstractEvent<String>{
    public TodoSyncEvent(String s) {
        super(s);
    }
}
