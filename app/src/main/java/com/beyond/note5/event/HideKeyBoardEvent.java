package com.beyond.note5.event;

/**
 * @author: beyond
 * @date: 2019/2/7
 */

public class HideKeyBoardEvent extends AbstractEvent<String> {

    private String type;

    public HideKeyBoardEvent(String s) {
        super(s);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
