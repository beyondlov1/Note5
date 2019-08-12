package com.beyond.note5.presenter;

import android.os.Handler;
import android.util.Log;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.Synchronizer;
import com.beyond.note5.view.SyncView;

import java.util.List;

public class NoteSyncPresenterImpl implements SyncPresenter {

    private SyncView syncView;

    private List<Synchronizer<Note>> synchronizers;

    private final Handler handler;

    public NoteSyncPresenterImpl(SyncView syncView, Handler handler) {
        this.syncView = syncView;
        this.handler = handler;
    }

    @Override
    public void sync() {
        synchronizers = MyApplication.getInstance().getNoteSynchronizers();
        MyApplication.getInstance().getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                StringBuilder msg = new StringBuilder();
                boolean success = true;
                for (Synchronizer<Note> synchronizer : synchronizers) {
                    try {
                        synchronizer.sync();
                    }catch (Exception e){
                        Log.e(getClass().getSimpleName(),"同步失败",e);
                        success = false;
                        msg.append(e.getMessage());
                        msg.append("\n");
                    }
                }
                if (success){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            syncView.onSyncSuccess("Note同步成功");
                        }
                    });
                }else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            msg.append("将于一小时内重试");
                            syncView.onSyncFail(msg.toString());
                        }
                    });
                }
            }
        });
    }
}
