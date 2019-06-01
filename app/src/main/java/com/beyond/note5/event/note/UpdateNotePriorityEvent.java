package com.beyond.note5.event.note;

import com.beyond.note5.bean.Note;
import com.beyond.note5.event.AbstractEvent;

public class UpdateNotePriorityEvent extends AbstractEvent<Note> {
    public UpdateNotePriorityEvent(Note note) {
        super(note);
    }
}
