package com.beyond.note5.presenter;

import com.beyond.note5.bean.Note;

import java.util.List;

/**
 * Created by beyond on 2019/1/31.
 */

public interface NotePresenter {

    void addNote(Note note);

    void addNoteSuccess(Note note);

    void addNoteFail(Note note);

    void updateNote(Note note);

    void updateNoteSuccess(Note note);

    void updateNoteFail(Note note);

    void deleteNote(Note note);

    void deleteNoteSuccess(Note note);

    void deleteNoteFail(Note note);

    void findAllNote();

    void findAllNoteSuccess(List<Note> allNote);

    void findAllNoteFail();
}
