package com.beyond.note5.view.markdown.decorate;

import android.text.Editable;

/**
 * @author: beyond
 * @date: 2019/7/19
 */

public class RichLine {

    private Editable fullSource;
    private int start;
    private int end;
    private String content;
    private int length;

    public RichLine(Editable fullSource, int start, int end) {
        this.fullSource = fullSource;
        this.start = start;
        this.end = end;

        int count = end - start;
        char[] chars = new char[count];
        fullSource.getChars(start,end,chars,0);
        StringBuilder stringBuilder = new StringBuilder();
        for (char aChar : chars) {
            stringBuilder.append(aChar);
        }
        content = stringBuilder.toString();
        length = content.length();
    }

    public Editable getFullSource() {
        return fullSource;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getContent() {
        return content;
    }

    public void deleteTag(String tag) {
        if (!startsWith(tag)){
            return;
        }
        int start = this.start;
        int end = start + tag.length();
        fullSource.delete(start,end);
    }

    public boolean startsWith(String tag){
        return content.startsWith(tag);
    }

    public int getTagStart(String tag) {
        return content.indexOf(tag);
    }

    public int getLength() {
        return length;
    }

    public <T> T[] getSpans(Class<T> clazz){
        if (start >= end){
            return null;
        }
        return fullSource.getSpans(start,end, clazz);
    }
}
