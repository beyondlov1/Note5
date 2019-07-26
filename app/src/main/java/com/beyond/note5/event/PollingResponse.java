package com.beyond.note5.event;

/**
 * @author: beyond
 * @date: 2019/7/25
 */

public class PollingResponse extends AbstractEvent<Object>{
    public PollingResponse(Object data) {
        super(data);
    }
}
