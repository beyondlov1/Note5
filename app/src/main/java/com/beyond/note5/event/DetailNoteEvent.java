package com.beyond.note5.event;

import com.beyond.note5.bean.Note;

import java.util.List;

/**
 * @author: beyond
 * @date: 2019/2/2
 */

public class DetailNoteEvent extends AbstractEvent<List<Note>> {

    private int index;

    public DetailNoteEvent(List<Note> notes, int index) {
        super(notes);
        this.index = index;
    }

    public int getIndex(){
        return index;
    }
}
