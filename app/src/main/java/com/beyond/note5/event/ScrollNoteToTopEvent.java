package com.beyond.note5.event;

import com.beyond.note5.bean.Note;

public class ScrollNoteToTopEvent extends AbstractEvent<Note>{
    public ScrollNoteToTopEvent(Note note) {
        super(note);
    }
}
