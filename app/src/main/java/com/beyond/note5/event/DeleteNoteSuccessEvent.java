package com.beyond.note5.event;

import com.beyond.note5.bean.Note;

/**
 * @author: beyond
 * @date: 2019/2/2
 */

public class DeleteNoteSuccessEvent extends AbstractEvent<Note> {
    public DeleteNoteSuccessEvent(Note note) {
        super(note);
    }
}
