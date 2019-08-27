package com.beyond.note5.component.module;

import android.os.Handler;

import com.beyond.note5.presenter.SyncPresenter;
import com.beyond.note5.presenter.TodoSyncPresenterImpl;
import com.beyond.note5.view.SyncView;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author: beyond
 * @date: 2019/8/27
 */
@Module
public class TodoSyncModule {

    private SyncView syncView;

    public TodoSyncModule(SyncView syncView) {
        this.syncView = syncView;
    }

    @Singleton
    @Provides
    @Inject
    SyncPresenter provideSyncPresenter(Handler handler) {
        return new TodoSyncPresenterImpl(syncView,handler);
    }

}
