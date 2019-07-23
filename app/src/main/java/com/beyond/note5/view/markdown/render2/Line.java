package com.beyond.note5.view.markdown.render2;

/**
 * @author: beyond
 * @date: 2019/7/22
 */

public class Line {
    private int start;
    private int end;
    private String content;
    private int textSize;

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }
}
