package com.beyond.note5.event;

import com.beyond.note5.bean.Note;

/**
 * @author: beyond
 * @date: 2019/2/5
 */

public class FillNoteModifyEvent extends AbstractEvent<Note> {
    public FillNoteModifyEvent(Note note) {
        super(note);
    }
}
