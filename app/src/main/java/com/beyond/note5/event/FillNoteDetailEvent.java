package com.beyond.note5.event;

import com.beyond.note5.bean.Note;
import com.beyond.note5.constant.LoadType;

import java.util.List;

/**
 * @author: beyond
 * @date: 2019/2/2
 */

public class FillNoteDetailEvent extends AbstractEvent<List<Note>> {

    private int index;
    private LoadType loadType;
    private boolean consumed;

    public FillNoteDetailEvent(List<Note> notes, int index) {
        super(notes);
        this.index = index;
    }

    public int getIndex(){
        return index;
    }

    public void setLoadType(LoadType loadType) {
        this.loadType = loadType;
    }

    public LoadType getLoadType() {
        return loadType;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }
}
