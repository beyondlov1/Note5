package com.beyond.note5.module;

import com.beyond.note5.view.ShareActivity;
import com.beyond.note5.view.adapter.AbstractNoteViewFragment;
import com.beyond.note5.view.fragment.NoteListFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author: beyond
 * @date: 2019/1/31
 */

@Singleton
@Component(modules = {NoteModule.class})
public interface NoteComponent {
    void inject(AbstractNoteViewFragment abstractNoteViewFragment);
    void inject(NoteListFragment noteListFragment);
    void inject(ShareActivity activity);
}
