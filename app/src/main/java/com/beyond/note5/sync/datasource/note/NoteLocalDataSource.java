package com.beyond.note5.sync.datasource.note;

import com.beyond.note5.bean.Note;
import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.presenter.NotePresenterImpl;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.view.adapter.view.NoteViewAdapter;

import java.io.IOException;
import java.util.List;

public class NoteLocalDataSource implements DataSource<Note> {

    private NotePresenter notePresenter;

    public NoteLocalDataSource() {
        this.notePresenter = new NotePresenterImpl(new MyNoteView());
    }

    public NoteLocalDataSource(NotePresenter notePresenter) {
        this.notePresenter = notePresenter;
    }

    @Override
    public void add(Note note) {
        notePresenter.add(note);
    }

    @Override
    public void delete(Note note) {
        notePresenter.delete(note);
    }

    @Override
    public void update(Note note) {
        notePresenter.update(note);
    }

    @Override
    public Note select(Note note) {
        throw new RuntimeException("暂不支持");
    }

    @Override
    public Note selectById(String id) throws IOException {
        throw new RuntimeException("暂不支持");
    }

    @Override
    public List<Note> selectAll() throws IOException {
        return notePresenter.selectAll();
    }

    @Override
    public void cover(List<Note> all) throws IOException {
        throw new RuntimeException("暂不支持");
    }

    @Override
    public Class clazz() {
        return Note.class;
    }

    private class MyNoteView extends NoteViewAdapter {
    }
}
