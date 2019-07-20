package com.beyond.note5.view.markdown.render.bean;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public class ListLine extends Line{
    private int listIndex;

    public ListLine(String source) {
        super(source);
    }

    public static boolean isListLine(String lineSource){
        return lineSource.matches("\\d+\\..*");
    }

    public int getListIndex() {
        return listIndex;
    }

    public void setListIndex(int listIndex) {
        this.listIndex = listIndex;
    }
}
