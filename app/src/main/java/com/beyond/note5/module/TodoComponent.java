package com.beyond.note5.module;

import com.beyond.note5.view.adapter.AbstractTodoFragment;
import com.beyond.note5.view.fragment.NoteDetailSuperFragment;
import com.beyond.note5.view.fragment.TodoEditFragment;
import com.beyond.note5.view.fragment.TodoListFragment;
import com.beyond.note5.view.fragment.TodoModifySuperFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {TodoModule.class,PredictModule.class})
public interface TodoComponent {
    void inject(AbstractTodoFragment abstractTodoFragment);
    void inject(TodoListFragment todoListFragment); // 一个目标只能拿一个Component注入
    void inject(TodoModifySuperFragment todoModifySuperFragment);
    void inject(TodoEditFragment todoEditFragment);

    void inject(NoteDetailSuperFragment fragment);
}
