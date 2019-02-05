package com.beyond.note5.event;

import com.beyond.note5.bean.Note;

import java.util.List;

/**
 * Created by beyond on 2019/2/2.
 */

public class DetailNoteEvent extends AbstractEvent<List<Note>> {

    private int position;

    public DetailNoteEvent(List<Note> notes, int position) {
        super(notes);
        this.position = position;
    }

    public int getPosition(){
        return position;
    }
}
