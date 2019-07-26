package com.beyond.note5.event;

/**
 * @author: beyond
 * @date: 2019/7/25
 */

public class PollRequest extends AbstractEvent<Class>{
    public PollRequest(Class s) {
        super(s);
    }
}
