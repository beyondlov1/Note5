package com.beyond.note5.module;

import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.presenter.NotePresenterImpl;
import com.beyond.note5.view.NoteView;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author: beyond
 * @date: 2019/1/31
 */

@Module
public class NoteModule {

    private NoteView noteView;

    @Inject
    public NoteModule(NoteView noteView){
        this.noteView = noteView;
    }

    @Singleton
    @Provides
    NotePresenter provideNotePresenter(){
        return new NotePresenterImpl(noteView);
    }
}
