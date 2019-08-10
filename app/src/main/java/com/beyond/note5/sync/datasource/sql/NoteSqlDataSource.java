package com.beyond.note5.sync.datasource.sql;


import com.beyond.note5.bean.Note;
import com.beyond.note5.presenter.DocumentPresenter;
import com.beyond.note5.presenter.NotePresenterImpl;
import com.beyond.note5.view.adapter.view.NoteViewAdapter;

public class NoteSqlDataSource extends DocumentSqlDataSource<Note> {

    @Override
    protected DocumentPresenter<Note> getDocumentPresenter() {
        return new NotePresenterImpl(new MyNoteView());
    }

    @Override
    public Class<Note> clazz() {
        return Note.class;
    }

    private class MyNoteView extends NoteViewAdapter {
    }
}
