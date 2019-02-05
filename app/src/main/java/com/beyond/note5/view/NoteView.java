package com.beyond.note5.view;

import com.beyond.note5.bean.Note;

import java.util.List;

/**
 * Created by beyond on 2019/1/31.
 */

public interface NoteView {

    void onAddNoteSuccess(Note note);

    void msg(String msg);

    void onFindAllNoteSuccess(List<Note> allNote);

    void deleteNoteFail(Note note);

    void deleteNoteSuccess(Note note);

    void updateNoteSuccess(Note note);

    void updateNoteFail(Note note);
}
