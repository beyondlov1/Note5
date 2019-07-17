package com.beyond.note5.event;

import com.beyond.note5.bean.Note;

import java.util.List;

/**
 * @author: beyond
 * @date: 2019/7/17
 */

public class AddNoteAllSuccessEvent extends AbstractEvent<List<Note>>{

    public AddNoteAllSuccessEvent(List<Note> notes) {
        super(notes);
    }
}
