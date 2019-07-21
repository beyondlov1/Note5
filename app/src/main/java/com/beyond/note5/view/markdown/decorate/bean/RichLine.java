package com.beyond.note5.view.markdown.decorate.bean;

import android.text.Editable;
import android.util.Log;

/**
 * @author: beyond
 * @date: 2019/7/19
 */

public class RichLine {

    private final Editable fullSource;
    private final String content;
    private final int start;
    private final int end;
    private final int length;
    private int index;
    private RichLine prev;
    private RichLine next;

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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public RichLine getPrev() {
        return prev;
    }

    public void setPrev(RichLine prev) {
        this.prev = prev;
    }

    public RichLine getNext() {
        return next;
    }

    public void setNext(RichLine next) {
        this.next = next;
    }

    public void deleteTag(String tag) {
        if (!startsWith(tag)){
            return;
        }
        Log.d(getClass().getSimpleName(),"deleteTag-start-"+System.currentTimeMillis());
        int start = this.start;
        int end = start + tag.length();
        fullSource.delete(start,end);
        Log.d(getClass().getSimpleName(),"deleteTag-end-"+System.currentTimeMillis());
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
