package com.beyond.note5.event;

import com.beyond.note5.bean.Note;

/**
 * @author: beyond
 * @date: 2019/2/2
 */

public class UpdateNoteSuccessEvent extends AbstractEvent<Note> {

    public UpdateNoteSuccessEvent(Note note) {
        super(note);
    }
}
