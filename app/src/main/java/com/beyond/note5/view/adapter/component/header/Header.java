package com.beyond.note5.view.adapter.component.header;

import com.beyond.note5.bean.Element;

public class Header implements Element {
    private int position;
    private String content;

    public Header(int position, String content) {
        this.position = position;
        this.content = content;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}