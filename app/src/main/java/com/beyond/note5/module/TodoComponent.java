package com.beyond.note5.module;

import com.beyond.note5.view.fragment.TodoListFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {TodoModule.class})
public interface TodoComponent {
    void inject(TodoListFragment todoListFragment); // 一个目标只能拿一个Component注入
}
