package com.beyond.note5.service.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.beyond.note5.service.schedule.callback.ScheduleCallback;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.lang.reflect.Method;

import static android.content.Context.ALARM_SERVICE;

/**
 * @author: beyond
 * @date: 2019/7/23
 */

public class ScheduleReceiver extends BroadcastReceiver {

    public static final long DEFAULT_SCHEDULE_PERIOD = 24 * 60 * 60 * 1000L;

    public static final int NOTIFICATION_SCAN_REQUEST_CODE = 1;
    public static final int SYNC_REQUEST_CODE = 2;

    @SuppressWarnings("unchecked")
    @Override
    public void onReceive(Context context, Intent intent) {
        String callbackClassName = intent.getStringExtra("callbackClass");
        if (callbackClassName == null){
            Log.e(getClass().getSimpleName(),"不支持");
            return;
        }
        Log.d(getClass().getSimpleName(),"定时任务开始:"+callbackClassName);
        try {
            Class<?> callbackClass = Class.forName(callbackClassName);
            Method callMethod = callbackClass.getDeclaredMethod("onCall", Context.class, Intent.class);
            Object o = callbackClass.newInstance();
            callMethod.setAccessible(true);
            callMethod.invoke(o,context,intent);

            int requestCode = intent.getIntExtra("requestCode", 0);
            long startTime = intent.getLongExtra("startTime", System.currentTimeMillis());
            long delay = intent.getLongExtra("delay", DEFAULT_SCHEDULE_PERIOD);
            schedule(context, requestCode,startTime, delay, (Class<ScheduleCallback>) callbackClass);
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(),"反射出错",e);
        }
        Log.d(getClass().getSimpleName(),"定时任务结束"+callbackClassName);

    }

    public static void schedule(Context context, int requestCode,Class<? extends ScheduleCallback> callbackClass) {
        schedule(context, requestCode, DEFAULT_SCHEDULE_PERIOD,callbackClass);
    }

    public static void schedule(Context context, int requestCode, long delay,Class<? extends ScheduleCallback> callbackClass) {
        schedule(context, requestCode, System.currentTimeMillis(), delay, callbackClass);
    }

    public static void schedule(Context context, int requestCode, long startTime, long delay,Class<? extends ScheduleCallback> callbackClass) {
        long current = System.currentTimeMillis();
        startTime += floorDiv((current - startTime), delay) * delay;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            long triggerTime = startTime + delay;
            Intent it = new Intent(context, ScheduleReceiver.class);
            it.putExtra("startTime", startTime);
            it.putExtra("delay", delay);
            it.putExtra("requestCode", requestCode);
            it.putExtra("callbackClass", callbackClass.getName());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, it, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            Log.d(ScheduleReceiver.class.getSimpleName(),
                    "定时任务已设定, 当前时间:" + DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss") +
                            "设定时间:" + DateFormatUtils.format(triggerTime, "yyyy-MM-dd HH:mm:ss"));
        }
    }

    private static long floorDiv(long x, long y) {
        long r = x / y;
        // if the signs are different and modulo not zero, round down
        if ((x ^ y) < 0 && (r * y != x)) {
            r--;
        }
        return r;
    }

    public static void cancel(Context context, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            Intent it = new Intent(context, ScheduleReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, it, PendingIntent.FLAG_NO_CREATE);
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
            Log.d(ScheduleReceiver.class.getSimpleName(), "定时任务已取消, 当前时间:" + DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
        }
    }


}
