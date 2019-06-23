package com.beyond.note5.presenter;

import android.os.Handler;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.Synchronizer;
import com.beyond.note5.view.SyncView;

public class NoteSyncPresenterImpl implements SyncPresenter {

    private SyncView syncView;

    private Synchronizer<Note> synchronizer;

    private final Handler handler;

    public NoteSyncPresenterImpl(SyncView syncView) {
        this.syncView = syncView;
        synchronizer = MyApplication.getInstance().getNoteSynchronizer();
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
