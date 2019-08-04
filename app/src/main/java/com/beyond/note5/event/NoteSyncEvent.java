package com.beyond.note5.event;

/**
 * @author: beyond
 * @date: 2019/8/4
 */

public class NoteSyncEvent extends AbstractEvent<String>{
    public NoteSyncEvent(String s) {
        super(s);
    }
}
