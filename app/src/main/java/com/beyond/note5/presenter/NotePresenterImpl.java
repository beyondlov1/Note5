package com.beyond.note5.presenter;

import com.beyond.note5.bean.Note;
import com.beyond.note5.model.NoteModel;
import com.beyond.note5.model.NoteModelImpl;
import com.beyond.note5.view.NoteView;

import java.util.List;

/**
 * @author: beyond
 * @date: 2019/1/31
 */

public class NotePresenterImpl implements NotePresenter {

    private NoteView noteView;
    private NoteModel noteModel;

    public NotePresenterImpl(NoteView noteView) {
        this.noteView = noteView;
        this.noteModel = new NoteModelImpl();
    }

    @Override
    public void add(Note note) {
        try {
            noteModel.add(note);
            this.addSuccess(note);
        }catch (Exception e){
            e.printStackTrace();
            this.addFail(note);
        }
    }

    @Override
    public void addSuccess(Note note) {
        noteView.onAddSuccess(note);
    }

    @Override
    public void addFail(Note note) {
        noteView.onAddFail(note);
    }

    @Override
    public void update(Note note) {
        try {
            noteModel.update(note);
            this.updateSuccess(note);
        }catch (Exception e){
            e.printStackTrace();
            this.updateFail(note);
        }
    }

    @Override
    public void updateSuccess(Note note) {
        noteView.onUpdateSuccess(note);
    }

    @Override
    public void updateFail(Note note) {
        noteView.onUpdateFail(note);
    }

    @Override
    public void delete(Note note) {
        try {
            noteModel.delete(note);
            this.deleteSuccess(note);
        }catch (Exception e){
            e.printStackTrace();
            this.deleteFail(note);
        }
    }

    @Override
    public void deleteDeep(Note note) {
        try {
            noteModel.deleteDeep(note);
            this.deleteSuccess(note);
        }catch (Exception e){
            e.printStackTrace();
            this.deleteFail(note);
        }
    }

    @Override
    public void deleteSuccess(Note note) {
        noteView.onDeleteSuccess(note);
    }

    @Override
    public void deleteFail(Note note) {
        noteView.onDeleteFail(note);
    }

    @Override
    public void findAll() {
        try {
            List<Note> allNote = noteModel.findAll();
            this.findAllSuccess(allNote);
        }catch (Exception e){
            e.printStackTrace();
            this.findAllFail();
        }
    }

    @Override
    public void findAllSuccess(List<Note> allNote) {
        noteView.onFindAllSuccess(allNote);
    }

    @Override
    public void findAllFail() {
        noteView.onFindAllFail();
    }
}
