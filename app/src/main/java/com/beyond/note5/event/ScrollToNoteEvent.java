package com.beyond.note5.event;

import com.beyond.note5.bean.Note;

public class ScrollToNoteEvent extends AbstractEvent<Note>{
    public ScrollToNoteEvent(Note note) {
        super(note);
    }
}
