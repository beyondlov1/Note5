package com.beyond.note5.presenter;

import android.os.Handler;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.sync.Synchronizer;
import com.beyond.note5.view.SyncView;

public class TodoSyncPresenterImpl implements SyncPresenter {

    private SyncView syncView;

    private Synchronizer<Todo> synchronizer;

    private final Handler handler;

    public TodoSyncPresenterImpl(SyncView syncView) {
        this.syncView = syncView;
        synchronizer = MyApplication.getInstance().getTodoSynchronizer();
        handler = new Handler();
    }

    @Override
    public void sync() {
        MyApplication.getInstance().getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronizer.sync();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            syncView.onSyncSuccess();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            syncView.onSyncFail();
                        }
                    });
                }
            }
        });
    }
}
