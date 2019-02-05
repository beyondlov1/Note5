package com.beyond.note5.event;

import com.beyond.note5.bean.Note;

/**
 * Created by beyond on 2019/2/2.
 */

public class UpdateNoteEvent extends AbstractEvent<Note> {

    public UpdateNoteEvent(Note note) {
        super(note);
    }

}
