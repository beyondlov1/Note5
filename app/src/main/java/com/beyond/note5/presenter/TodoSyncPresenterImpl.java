package com.beyond.note5.presenter;

import android.os.Handler;
import android.util.Log;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.sync.Synchronizer;
import com.beyond.note5.view.SyncView;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public class TodoSyncPresenterImpl implements SyncPresenter {

    private SyncView syncView;

    private List<Synchronizer<Todo>> synchronizers;

    private final Handler handler;

    public TodoSyncPresenterImpl(SyncView syncView) {
        this.syncView = syncView;
        handler = new Handler();
    }

    @Override
    public void sync() {
        if (CollectionUtils.isEmpty(synchronizers)){
            synchronizers = MyApplication.getInstance().getTodoSynchronizers();
        }
        MyApplication.getInstance().getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    for (Synchronizer<Todo> synchronizer : synchronizers) {
                        try {
                            synchronizer.sync();
                        }catch (Exception e){
                            Log.e(getClass().getSimpleName(),"同步失败",e);
                        }
                    }
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
