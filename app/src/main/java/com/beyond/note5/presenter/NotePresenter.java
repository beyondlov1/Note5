package com.beyond.note5.presenter;

import com.beyond.note5.bean.Note;

/**
 * @author: beyond
 * @date: 2019/1/31
 */

public interface NotePresenter extends DocumentPresenter<Note>{
    void deleteDeep(Note note);
    void deleteDeepLogic(Note note);
}
