package com.beyond.note5.module;

import com.beyond.note5.view.fragment.TodoEditFragment;
import com.beyond.note5.view.fragment.TodoModifySuperFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author beyondlov1
 * @date 2019/03/30
 */
@Singleton
@Component(modules = {PredictModule.class})
public interface PredictComponent {
    void inject(TodoModifySuperFragment todoModifySuperFragment);
    void inject(TodoEditFragment todoEditFragment);
}
