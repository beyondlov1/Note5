package com.beyond.note5.presenter;

import com.beyond.note5.bean.Note;
import com.beyond.note5.model.NoteModel;
import com.beyond.note5.model.NoteModelImpl;
import com.beyond.note5.view.NoteView;

import java.util.List;

/**
 * Created by beyond on 2019/1/31.
 */

public class NotePresenterImpl implements NotePresenter {

    private NoteView noteView;
    private NoteModel noteModel;

    public NotePresenterImpl(NoteView noteView) {
        this.noteView = noteView;
        this.noteModel = new NoteModelImpl();
    }

    @Override
    public void addNote(Note note) {
        try {
            noteModel.addNote(note);
            this.addNoteSuccess(note);
        }catch (Exception e){
            e.printStackTrace();
            this.addNoteFail(note);
        }
    }

    @Override
    public void addNoteSuccess(Note note) {
        noteView.onAddNoteSuccess(note);
    }

    @Override
    public void addNoteFail(Note note) {
        noteView.msg("添加失败");
    }

    @Override
    public void updateNote(Note note) {
        try {
            noteModel.updateNote(note);
            this.updateNoteSuccess(note);
        }catch (Exception e){
            e.printStackTrace();
            this.updateNoteFail(note);
        }
    }

    @Override
    public void updateNoteSuccess(Note note) {
        noteView.updateNoteSuccess(note);
    }

    @Override
    public void updateNoteFail(Note note) {
        noteView.updateNoteFail(note);
    }

    @Override
    public void deleteNote(Note note) {
        try {
            noteModel.deleteNote(note);
            this.deleteNoteSuccess(note);
        }catch (Exception e){
            e.printStackTrace();
            this.deleteNoteFail(note);
        }
    }

    @Override
    public void deleteNoteSuccess(Note note) {
        noteView.deleteNoteSuccess(note);
    }

    @Override
    public void deleteNoteFail(Note note) {
        noteView.deleteNoteFail(note);
    }

    @Override
    public void findAllNote() {
        try {
            List<Note> allNote = noteModel.findAllNote();
            this.findAllNoteSuccess(allNote);
        }catch (Exception e){
            e.printStackTrace();
            this.findAllNoteFail();
        }
    }

    @Override
    public void findAllNoteSuccess(List<Note> allNote) {
        noteView.onFindAllNoteSuccess(allNote);
    }

    @Override
    public void findAllNoteFail() {
        noteView.msg("查询失败");
    }
}
