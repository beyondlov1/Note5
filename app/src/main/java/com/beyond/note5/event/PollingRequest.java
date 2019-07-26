package com.beyond.note5.event;

/**
 * @author: beyond
 * @date: 2019/7/25
 */

public class PollingRequest extends AbstractEvent<Class>{
    public PollingRequest(Class s) {
        super(s);
    }
}
