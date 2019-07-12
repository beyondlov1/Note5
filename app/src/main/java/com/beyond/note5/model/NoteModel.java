package com.beyond.note5.model;

import com.beyond.note5.bean.Note;

import java.util.List;

/**
 * @author: beyond
 * @date: 2019/1/31
 */

public interface NoteModel extends DocumentModel<Note>{
    void deleteDeep(Note note);
    void deleteDeepLogic(Note note);
    List<Note> findByPriority(int priority);
}
