package com.beyond.note5.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: beyond
 * @date: 2019/7/17
 */

public class SyncRetryService extends Service {

    public static final String SYNC_RETRY_ACTION = "com.beyond.note5.intent.action.SYNC_RETRY";

    public static final int DEFAULT_RETRY_DELAY = 1000 * 60 * 40;

    private static final int MAX_FAIL_COUNT = 7;

    private static int failCount = 0;  // TODO: 1. 线程安全问题 2. 不同的数据源同步失败次数不同的问题

    private static AtomicLong nextRetryTimeMillis = null;

    public static void retry(Context context) {
        retry(context, DEFAULT_RETRY_DELAY);
    }

    public static void retry(Context context, long delay) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            long triggerTime = SystemClock.elapsedRealtime() + delay;
            Intent it = new Intent(context, SyncRetryReceiver.class);
            it.setAction(SYNC_RETRY_ACTION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, it, 0);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME, triggerTime, pendingIntent);
            Log.d(SyncRetryService.class.getSimpleName(), "重试任务已设定, 当前时间:" + DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
        }
    }

    public static void retryIfNecessary(Context context, long delay) {
        long currentTimeMillis = System.currentTimeMillis();
        if (nextRetryTimeMillis == null || nextRetryTimeMillis.get() < currentTimeMillis) {
            SyncRetryService.retry(context, delay);
            nextRetryTimeMillis.set(currentTimeMillis + delay);
        }
    }

    public static void failed() {
        failCount++;
    }

    public static void resetRetryFailCount() {
        failCount = 0;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Deprecated
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            long triggerTime = SystemClock.elapsedRealtime() + 1000 * 60 * 40;
            Intent it = new Intent(this, SyncRetryReceiver.class);
            it.setAction(SYNC_RETRY_ACTION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, it, 0);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME, triggerTime, pendingIntent);
            Log.d(getClass().getSimpleName(), "重试任务已设定, 当前时间:" + DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
        }
        return super.onStartCommand(intent, flags, startId);
    }


}
