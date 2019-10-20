package com.beyond.note5.component;

import com.beyond.note5.component.module.CalendarModule;
import com.beyond.note5.component.module.CommonModule;
import com.beyond.note5.component.module.PredictModule;
import com.beyond.note5.component.module.TodoCompositeModule;
import com.beyond.note5.component.module.TodoModule;
import com.beyond.note5.component.module.TodoSyncModule;
import com.beyond.note5.view.fragment.TodoListFragment;
import com.beyond.note5.view.fragment.TodoSearchResultFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author: beyond
 * @date: 2019/8/27
 */
@Singleton
@Component(modules = {CommonModule.class, TodoCompositeModule.class,
        TodoModule.class,CalendarModule.class, PredictModule.class,TodoSyncModule.class})
public interface TodoListFragmentComponent {
    void inject(TodoListFragment fragment);
    void inject(TodoSearchResultFragment fragment);
}
