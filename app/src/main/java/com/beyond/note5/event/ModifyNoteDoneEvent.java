package com.beyond.note5.event;

import com.beyond.note5.bean.Note;

/**
 * Created by beyond on 2019/2/5.
 */

public class ModifyNoteDoneEvent extends AbstractEvent<Note>{

    public ModifyNoteDoneEvent(Note note) {
        super(note);
    }
}
