package com.beyond.note5.view.markdown.span.bean;

import org.apache.commons.lang3.StringUtils;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public class Line {
    private int index;
    private String source;

    public Line(String source) {
        this.source = source;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean startWith(String prefix) {
        return StringUtils.startsWith(source,prefix);
    }

    public int getTagEnd(String tag) {
        return source.indexOf(tag)+tag.length();
    }

    public String getContentWithoutTag(String tag) {
        return StringUtils.trim(source.replaceFirst(tag, ""));
    }

    public int getTagStart(String tag) {
        return source.indexOf(tag);
    }
}
