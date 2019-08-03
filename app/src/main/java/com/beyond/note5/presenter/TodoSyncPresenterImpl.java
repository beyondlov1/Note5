package com.beyond.note5.presenter;

import android.os.Handler;
import android.util.Log;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.sync.Synchronizer;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.view.SyncView;

import java.util.List;

public class TodoSyncPresenterImpl implements SyncPresenter {

    private SyncView syncView;

    private List<Synchronizer<Todo>> synchronizers;

    private final Handler handler;

    public TodoSyncPresenterImpl(SyncView syncView, Handler handler) {
        this.syncView = syncView;
        this.handler = handler;
    }

    @Override
    public void sync() {
        ToastUtil.toast(MyApplication.getInstance(), "开始同步");
        synchronizers = MyApplication.getInstance().getTodoSynchronizers();
        MyApplication.getInstance().getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                StringBuilder msg = new StringBuilder();
                boolean success = true;
                for (Synchronizer<Todo> synchronizer : synchronizers) {
                    try {
                        synchronizer.sync();
                    } catch (Exception e) {
                        Log.e(getClass().getSimpleName(), "同步失败", e);
                        success = false;
                        msg.append(e.getMessage());
                        msg.append("\n");
                    }
                }

                if (success) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            syncView.onSyncSuccess("Todo同步成功");
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            syncView.onSyncFail(msg.toString());
                        }
                    });
                }
            }
        });
    }
}
