package com.beyond.note5.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;

/**
 * @author: beyond
 * @date: 2019/7/17
 */

public class SyncRetryService extends Service {

    public static final String SYNC_RETRY_ACTION = "com.beyond.note5.intent.action.SYNC_RETRY";

    public static void retry(Context context){
        Intent intent = new Intent(context,SyncRetryService.class);
        context.startActivity(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager!=null){
            long triggerTime = SystemClock.elapsedRealtime() + 1000 * 60 * 40;
            Intent it = new Intent(this,SyncRetryReceiver.class);
            it.setAction(SYNC_RETRY_ACTION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,it,0);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME, triggerTime,pendingIntent);
        }
        return super.onStartCommand(intent, flags, startId);
    }
}