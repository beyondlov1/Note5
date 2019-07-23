package com.beyond.note5.service.schedule.callback;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.beyond.note5.MyApplication;

public class SyncScheduleCallback implements ScheduleCallback {

    @Override
    public void onCall(Context context, Intent intent) {
        Log.d(getClass().getSimpleName(), "定时同步开启");
        MyApplication.getInstance().syncAllWithToast();
    }
}