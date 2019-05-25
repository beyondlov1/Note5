package com.beyond.note5.module;

import com.beyond.note5.view.fragment.FloatEditorFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {DocumentCompositeModule.class})
public interface DocumentCompositeComponent {
    void inject(FloatEditorFragment floatEditorFragment);
}
