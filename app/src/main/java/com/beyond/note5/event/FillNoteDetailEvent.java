package com.beyond.note5.event;

import com.beyond.note5.bean.Note;

import java.util.List;

/**
 * @author: beyond
 * @date: 2019/2/2
 */

public class FillNoteDetailEvent extends AbstractEvent<List<Note>> {

    private int index;
    private ShowNoteDetailEvent.ShowType showType;
    private boolean consumed;

    public FillNoteDetailEvent(List<Note> notes, int index) {
        super(notes);
        this.index = index;
    }

    public int getIndex(){
        return index;
    }

    public void setShowType(ShowNoteDetailEvent.ShowType showType) {
        this.showType = showType;
    }

    public ShowNoteDetailEvent.ShowType getShowType() {
        return showType;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }
}
