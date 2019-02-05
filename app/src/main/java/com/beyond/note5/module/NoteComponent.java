package com.beyond.note5.module;

import com.beyond.note5.view.ShareActivity;
import com.beyond.note5.view.fragment.NoteListFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by beyond on 2019/1/31.
 */

@Singleton
@Component(modules = {NoteModule.class})
public interface NoteComponent {
    void inject(NoteListFragment noteListFragment);
    void inject(ShareActivity activity);
}
