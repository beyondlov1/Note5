package com.beyond.note5.view.markdown.decorate.bean;

import android.text.Editable;

import com.beyond.note5.view.markdown.decorate.MarkdownDecorator;

/**
 * @author: beyond
 * @date: 2019/7/19
 */

public class RichListLine extends RichLine {

    private int listIndex;

    public RichListLine(RichLine richLine) {
        super(richLine.getFullSource(), richLine.getStart(), richLine.getEnd());
    }

    public RichListLine(Editable fullSource, int start, int end) {
        super(fullSource, start, end);
    }

    public int getListIndex() {
        return listIndex;
    }

    public void setListIndex(int listIndex) {
        this.listIndex = listIndex;
    }

    public static boolean isOlListLine(String lineSource){
        return lineSource.matches("\\d+\\..*\\n?");
    }

    public static boolean isUlListLine(String lineSource) {
        return lineSource.startsWith(MarkdownDecorator.UL);
    }
}
