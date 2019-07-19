package com.beyond.note5.view.markdown.decorate;

import android.text.Editable;

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

    public static boolean isListLine(String lineSource){
        return lineSource.matches("\\d+\\..*\\n?");
    }

    public int getListIndex() {
        return listIndex;
    }

    public void setListIndex(int listIndex) {
        this.listIndex = listIndex;
    }
}
