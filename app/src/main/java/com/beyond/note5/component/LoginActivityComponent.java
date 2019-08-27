package com.beyond.note5.component;

import com.beyond.note5.component.module.AccountModule;
import com.beyond.note5.component.module.CommonModule;
import com.beyond.note5.view.LoginActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author: beyond
 * @date: 2019/8/27
 */
@Singleton
@Component(modules = {CommonModule.class,AccountModule.class})
public interface LoginActivityComponent {
    void inject(LoginActivity activity);
}
