package com.beyond.note5.module;

import com.beyond.note5.config.SyncProperty;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PropertyModule {

    @Singleton
    @Provides
    SyncProperty provideSyncProperty(){
        return new SyncProperty();
    }
}
