package com.beyond.note5.event;

import com.beyond.note5.bean.Note;

/**
 * @author: beyond
 * @date: 2019/2/2
 */

public class AddNoteEvent extends AbstractEvent<Note> {

    public AddNoteEvent(Note note) {
        super(note);
    }

}
