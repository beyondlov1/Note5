package com.beyond.note5.event.note;

import com.beyond.note5.bean.Note;
import com.beyond.note5.event.AbstractEvent;

/**
 * @author: beyond
 * @date: 2019/2/2
 */

public class AddNoteSuccessEvent extends AbstractEvent<Note> {

    public AddNoteSuccessEvent(Note note) {
        super(note);
    }

}
