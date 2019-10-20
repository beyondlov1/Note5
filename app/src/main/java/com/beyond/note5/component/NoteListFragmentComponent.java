package com.beyond.note5.component;

import com.beyond.note5.component.module.CommonModule;
import com.beyond.note5.component.module.NoteModule;
import com.beyond.note5.component.module.NoteSyncModule;
import com.beyond.note5.view.fragment.NoteListFragment;
import com.beyond.note5.view.fragment.NoteSearchResultFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author: beyond
 * @date: 2019/8/27
 */
@Singleton
@Component(modules = {CommonModule.class,NoteModule.class,NoteSyncModule.class})
public interface NoteListFragmentComponent {
    void inject(NoteListFragment target);
    void inject(NoteSearchResultFragment target);
}
