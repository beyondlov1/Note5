package com.beyond.note5.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.beyond.note5.MyApplication;

public class SyncRetryReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(getClass().getSimpleName(),"开始重试同步");
        MyApplication.getInstance().syncAllWithToast();
    }
}