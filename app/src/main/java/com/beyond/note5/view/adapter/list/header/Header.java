package com.beyond.note5.view.adapter.list.header;

import com.beyond.note5.bean.Element;
import com.beyond.note5.utils.IDUtil;

public class Header implements Element {

    private String id;
    private int position;
    private String content;

    public Header(int position, String content) {
        this.position = position;
        this.content = content;
        this.id = IDUtil.uuid();
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

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
}