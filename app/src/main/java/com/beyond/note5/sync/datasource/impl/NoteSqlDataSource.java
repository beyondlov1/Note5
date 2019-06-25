package com.beyond.note5.sync.datasource.impl;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Note;
import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.presenter.NotePresenterImpl;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.utils.PreferenceUtil;
import com.beyond.note5.view.adapter.view.NoteViewAdapter;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class NoteSqlDataSource implements DataSource<Note> {

    private NotePresenter notePresenter;

    public NoteSqlDataSource() {
        this.notePresenter = new NotePresenterImpl(new MyNoteView());
    }

    public NoteSqlDataSource(NotePresenter notePresenter) {
        this.notePresenter = notePresenter;
    }

    @Override
    public String getKey() {
        return PreferenceUtil.getString(MyApplication.VIRTUAL_USER_ID);
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
        return notePresenter.selectById(id);
    }

    @Override
    public List<Note> selectAll() throws IOException {
        return notePresenter.selectAllInAll();
    }

    @Override
    public List<Note> selectByModifiedDate(Date date) throws IOException {
        return notePresenter.selectByModifiedDate(date);
    }

    @Override
    public void cover(List<Note> all) throws IOException {
        throw new RuntimeException("暂不支持");
    }

    @Override
    public Class<Note> clazz() {
        return Note.class;
    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(Long time) {
        return false;
    }

    @Override
    public boolean isLocked() {
        return false;
    }

    @Override
    public boolean release() {
        return false;
    }

    private class MyNoteView extends NoteViewAdapter {
    }
}
