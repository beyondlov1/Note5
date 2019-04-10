package com.beyond.note5.event;

public class HideNoteDetailEvent extends AbstractEvent<Integer> {
    private int firstIndex;

    public HideNoteDetailEvent(int s) {
        super(s);
    }

    public void setFirstIndex(int firstIndex) {
        this.firstIndex = firstIndex;
    }

    public int getFirstIndex() {
        return firstIndex;
    }
}
