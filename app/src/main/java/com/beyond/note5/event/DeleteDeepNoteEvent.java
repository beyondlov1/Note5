package com.beyond.note5.event;

import com.beyond.note5.bean.Note;

/**
 * @author beyondlov1
 * @date 2019/03/24
 */
public class DeleteDeepNoteEvent extends AbstractEvent<Note>{
    public DeleteDeepNoteEvent(Note note) {
        super(note);
    }
}
