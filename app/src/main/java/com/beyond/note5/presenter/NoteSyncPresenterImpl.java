package com.beyond.note5.presenter;

import android.os.Handler;
import android.util.Log;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.Synchronizer;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.view.SyncView;

import org.apache.commons.collections4.CollectionUtils;

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
        ToastUtil.toast(MyApplication.getInstance(), "开始同步");

        if (CollectionUtils.isEmpty(synchronizers)){
            synchronizers = MyApplication.getInstance().getNoteSynchronizers();
        }

        MyApplication.getInstance().getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                for (Synchronizer<Note> synchronizer : synchronizers) {
                    try {
                        synchronizer.sync();
                    }catch (Exception e){
                        Log.e(getClass().getSimpleName(),"同步失败",e);
                        success = false;
                    }
                }
                if (success){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            syncView.onSyncSuccess();

                        }
                    });
                }else {
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
