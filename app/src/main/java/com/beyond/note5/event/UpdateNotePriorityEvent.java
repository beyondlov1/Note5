package com.beyond.note5.event;

import com.beyond.note5.bean.Note;

public class UpdateNotePriorityEvent extends AbstractEvent<Note>{

    public UpdateNotePriorityEvent(Note note) {
        super(note);
    }
}
