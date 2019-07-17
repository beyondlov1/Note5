package com.beyond.note5.sync.datasource.impl;


import com.beyond.note5.bean.Note;
import com.beyond.note5.event.AbstractEvent;
import com.beyond.note5.event.note.AddNoteSuccessEvent;
import com.beyond.note5.event.note.DeleteNoteSuccessEvent;
import com.beyond.note5.presenter.DocumentPresenter;
import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.presenter.NotePresenterImpl;
import com.beyond.note5.view.adapter.view.NoteViewAdapter;

public class NoteSqlDataSource extends DocumentSqlDataSource<Note> {

    public NoteSqlDataSource() {
        super();
    }

    public NoteSqlDataSource(NotePresenter notePresenter) {
        super(notePresenter);
    }

    @Override
    protected DocumentPresenter<Note> getDocumentPresenter() {
        return new NotePresenterImpl(new MyNoteView());
    }

    @Override
    protected AbstractEvent<Note> getAddSuccessEvent(Note note) {
        return new AddNoteSuccessEvent(note);
    }

    @Override
    protected AbstractEvent<Note> getDeleteSuccessEvent(Note note) {
        return new DeleteNoteSuccessEvent(note);
    }

    @Override
    public Class<Note> clazz() {
        return Note.class;
    }

    private class MyNoteView extends NoteViewAdapter {
    }
}
