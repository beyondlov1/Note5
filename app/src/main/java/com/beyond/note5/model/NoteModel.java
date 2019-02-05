package com.beyond.note5.model;

import com.beyond.note5.bean.Note;

import java.util.List;

/**
 * Created by beyond on 2019/1/31.
 */

public interface NoteModel {

    void addNote(Note note);

    void updateNote(Note note);

    void deleteNote(Note note);

    List<Note> findAllNote();

}
