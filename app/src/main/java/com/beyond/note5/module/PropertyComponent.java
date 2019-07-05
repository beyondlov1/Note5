package com.beyond.note5.module;

import com.beyond.note5.sync.datasource.impl.NoteDavDataSourceWrap;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {PropertyModule.class})
public interface PropertyComponent {
    void inject(NoteDavDataSourceWrap syncPropertyAware);
}
