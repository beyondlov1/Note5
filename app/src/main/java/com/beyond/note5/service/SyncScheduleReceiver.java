package com.beyond.note5.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.beyond.note5.MyApplication;

import org.apache.commons.lang3.time.DateFormatUtils;

import static android.content.Context.ALARM_SERVICE;

public class SyncScheduleReceiver extends BroadcastReceiver {

    private static final String SYNC_SCHEDULE_ACTION = "com.beyond.note5.intent.action.SYNC_SCHEDULE";

    public static final long DEFAULT_SCHEDULE_PERIOD = 24 * 60 * 60 * 1000L;

    private static long delay = DEFAULT_SCHEDULE_PERIOD;

    private static long startTime = System.currentTimeMillis();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(getClass().getSimpleName(), "定时同步开启");
        MyApplication.getInstance().syncAllWithToast();
        schedule(context, startTime, delay);
    }

    public static void schedule(Context context) {
        schedule(context, DEFAULT_SCHEDULE_PERIOD);
    }

    public static void schedule(Context context, long delay) {
        schedule(context, System.currentTimeMillis(), delay);
    }

    public static void schedule(Context context, long startTime, long delay) {
        long current = System.currentTimeMillis();
        startTime += floorDiv((current - startTime), delay) * delay;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            long triggerTime = startTime + delay;
            Intent it = new Intent(context, SyncScheduleReceiver.class);
            it.setAction(SYNC_SCHEDULE_ACTION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, it, 0);
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            Log.d(SyncScheduleReceiver.class.getSimpleName(),
                    "定时任务已设定, 当前时间:" + DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss") +
                            "设定时间:" + DateFormatUtils.format(triggerTime, "yyyy-MM-dd HH:mm:ss"));
        }
        SyncScheduleReceiver.delay = delay;
        SyncScheduleReceiver.startTime = startTime;
    }

    private static long floorDiv(long x, long y) {
        long r = x / y;
        // if the signs are different and modulo not zero, round down
        if ((x ^ y) < 0 && (r * y != x)) {
            r--;
        }
        return r;
    }

    public static void cancel(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            Intent it = new Intent(context, SyncScheduleReceiver.class);
            it.setAction(SYNC_SCHEDULE_ACTION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, it, 0);
            alarmManager.cancel(pendingIntent);
            Log.d(SyncScheduleReceiver.class.getSimpleName(), "定时任务已取消, 当前时间:" + DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
        }
    }

}