package com.beyond.note5.component;

import com.beyond.note5.MyApplication;
import com.beyond.note5.component.module.CommonModule;
import com.beyond.note5.presenter.AccountPresenterImpl;
import com.beyond.note5.presenter.NotePresenterImpl;
import com.beyond.note5.presenter.PredictPresenterImpl;
import com.beyond.note5.view.fragment.AbstractTodoEditorFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author: beyond
 * @date: 2019/8/27
 */
@Singleton
@Component(modules = {CommonModule.class})
public interface CommonComponent {
    void inject(MyApplication target);
    void inject(AbstractTodoEditorFragment target);
    void inject(NotePresenterImpl target);
    void inject(PredictPresenterImpl target);
    void inject(AccountPresenterImpl accountPresenter);
}
