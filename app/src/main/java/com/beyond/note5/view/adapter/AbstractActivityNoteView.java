package com.beyond.note5.view.adapter;

import com.beyond.note5.bean.Note;
import com.beyond.note5.view.NoteView;

/**
 * @author: beyond
 * @date: 2019/2/6
 */

public abstract class AbstractActivityNoteView extends AbstractActivityDocumentView<Note> implements NoteView {
    @Override
    public void onUpdatePrioritySuccess(Note note){

    }

    @Override
    public void onUpdatePriorityFail(Note note) {
        msg("更新失败");
    }
}
