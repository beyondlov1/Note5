package com.beyond.note5.service.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.beyond.note5.service.schedule.callback.ScheduleCallback;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.lang.reflect.Method;
import java.util.Map;

import static android.content.Context.ALARM_SERVICE;

/**
 * @author: beyond
 * @date: 2019/7/23
 */

public class ScheduleReceiver extends BroadcastReceiver {

    public static final long DEFAULT_SCHEDULE_PERIOD = 24 * 60 * 60 * 1000L;

    public static final int NOTIFICATION_EXACT_REQUEST_CODE = 0;
    public static final int NOTIFICATION_SCAN_REQUEST_CODE = 1;
    public static final int SYNC_REQUEST_CODE = 2;
    public static final String ID_PREFIX = "beyond://beyond.com/";
    public static final String DEFAULT_ID = "DEFAULT_ID";

    @SuppressWarnings("unchecked")
    @Override
    public void onReceive(Context context, Intent intent) {
        String callbackClassName = intent.getStringExtra("_callbackClass");
        if (callbackClassName == null) {
            Log.e(getClass().getSimpleName(), "不支持");
            return;
        }
        Log.d(getClass().getSimpleName(), "定时任务开始:" + callbackClassName);
        try {
            Class<?> callbackClass = Class.forName(callbackClassName);
            Method callMethod = callbackClass.getDeclaredMethod("onCall", Context.class, Intent.class);
            Object o = callbackClass.newInstance();
            callMethod.setAccessible(true);
            callMethod.invoke(o, context, intent);
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "反射出错", e);
        }
        Log.d(getClass().getSimpleName(), "定时任务结束" + callbackClassName);

    }

    public static void scheduleOnce(Context context, int requestCode,
                                    long startTime, @NonNull Class<? extends ScheduleCallback> callbackClass,
                                    @Nullable Map<String, String> data,
                                    String id) {
        scheduleOnce(context,requestCode,startTime,callbackClass.getName(),data,id);
    }

    public static void scheduleOnce(Context context, int requestCode,
                                    long startTime, @NonNull String callbackClassName,
                                    @Nullable Map<String, String> data,
                                    String id) {
        long current = System.currentTimeMillis();
        if (current > startTime) {
            return;
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            Intent it = new Intent(context, ScheduleReceiver.class);
            it.setData(Uri.parse(ID_PREFIX + id));
            it.putExtra("_startTime", startTime);
            it.putExtra("_requestCode", requestCode);
            it.putExtra("_callbackClass", callbackClassName);
            if (data != null) {
                for (String name : data.keySet()) {
                    it.putExtra(name, data.get(name));
                }
            }
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, it, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.set(AlarmManager.RTC_WAKEUP, startTime, pendingIntent);
            Log.d(ScheduleReceiver.class.getSimpleName(),
                    "定时任务已设定, 当前时间:" + DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss") +
                            "设定时间:" + DateFormatUtils.format(startTime, "yyyy-MM-dd HH:mm:ss"));
        }
    }

    public static void scheduleRepeat(Context context, int requestCode, long startTime, int period, Class<? extends ScheduleCallback> callbackClass) {
        scheduleRepeat(context,requestCode,startTime,period,callbackClass.getName(),null,DEFAULT_ID);
    }

    public static void scheduleRepeat(Context context, int requestCode,
                                      long startTime, long period, @NonNull String callbackClassName,
                                      @Nullable Map<String, String> data,
                                      String scheduleId) {
        // 如果设置的时间过早, 则找最近的应该发生的时间
        long current = System.currentTimeMillis();
        if (current > startTime) {
            startTime += floorDiv((current - startTime), period) * period;
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            long triggerTime = startTime + period;
            Intent it = new Intent(context, ScheduleReceiver.class);
            it.setData(Uri.parse(ID_PREFIX + scheduleId));
            it.putExtra("_startTime", startTime);
            it.putExtra("_period", period);
            it.putExtra("_requestCode", requestCode);
            it.putExtra("_callbackClass", callbackClassName);
            if (data != null) {
                for (String name : data.keySet()) {
                    it.putExtra(name, data.get(name));
                }
            }
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, it, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, period, pendingIntent);
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

    public static void cancel(Context context, int requestCode, String scheduleId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            Intent it = new Intent(context, ScheduleReceiver.class);
            it.setData(Uri.parse(ID_PREFIX + scheduleId));
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, it, PendingIntent.FLAG_NO_CREATE);
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
            Log.d(ScheduleReceiver.class.getSimpleName(), "定时任务已取消, 当前时间:" + DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
        }
    }



}
