package com.beyond.note5.view;

import com.beyond.note5.bean.Note;

/**
 * @author: beyond
 * @date: 2019/1/31
 */

public interface NoteView extends DocumentView<Note>{

    void updatePrioritySuccess(Note note);

    void updatePriorityFail(Note note);
}
