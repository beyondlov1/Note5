package com.beyond.note5.component.module;

import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.presenter.NotePresenterImpl;
import com.beyond.note5.view.NoteView;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author: beyond
 * @date: 2019/8/27
 */
@Module
public class NoteModule {

    private NoteView noteView;

    public NoteModule(NoteView noteView) {
        this.noteView = noteView;
    }

    @Singleton
    @Provides
    NotePresenter provideNotePresenter() {
        return new NotePresenterImpl(noteView);
    }

}
