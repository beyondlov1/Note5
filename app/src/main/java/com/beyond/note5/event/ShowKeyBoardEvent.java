package com.beyond.note5.event;

/**
 * @author: beyond
 * @date: 2019/2/4
 */

public class ShowKeyBoardEvent extends AbstractEvent<Integer> {

    private String type;

    public ShowKeyBoardEvent(Integer y) {
        super(y);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
