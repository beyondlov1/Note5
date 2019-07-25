package com.beyond.note5.service.schedule.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Note;
import com.beyond.note5.service.schedule.ScheduleReceiver;
import com.beyond.note5.service.schedule.callback.NoteExactNotifyScheduleCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.beyond.note5.service.schedule.ScheduleReceiver.ID_PREFIX;
import static com.beyond.note5.service.schedule.ScheduleReceiver.NOTIFICATION_EXACT_REQUEST_CODE;
import static com.beyond.note5.service.schedule.callback.NoteNotifyScheduleCallback.NOTIFICATION_POINTS;

/**
 * @author: beyond
 * @date: 2019/7/25
 */

public class ScheduleUtil {

    private static void scheduleNotificationFromNow(Note note) {
        scheduleNotificationFrom(note, System.currentTimeMillis());
    }

    public static void scheduleNotificationFrom(Note note, long timeMillis) {
        if (note.getPriority() == 5) {
            startScheduleFrom(note, timeMillis);
        } else {
            cancelSchedule(note);
        }
    }

    private static void startScheduleFrom(Note note, long timeMillis) {
        Map<String, String> data = new HashMap<>(2);
        data.put("id", note.getId());
        data.put("index", String.valueOf(0));
        List<String> scheduleIds = getScheduleIds(note);
        for (int i = 0; i < scheduleIds.size(); i++) {
            ScheduleReceiver.scheduleOnce(MyApplication.getInstance(), NOTIFICATION_EXACT_REQUEST_CODE,
                    timeMillis + NOTIFICATION_POINTS[i] * 60 * 1000,
                    NoteExactNotifyScheduleCallback.class,
                    data, scheduleIds.get(i));
        }
    }

    public static void cancelSchedule(Note note) {
        List<String> scheduleIds = getScheduleIds(note);
        for (String scheduleId : scheduleIds) {
            ScheduleReceiver.cancel(MyApplication.getInstance(), NOTIFICATION_EXACT_REQUEST_CODE, scheduleId);
        }
    }

    private static List<String> getScheduleIds(Note note) {
        List<String> scheduleIs = new ArrayList<>(NOTIFICATION_POINTS.length);
        for (long notificationPoint : NOTIFICATION_POINTS) {
            scheduleIs.add(note.getId() + "/" + notificationPoint);
        }
        return scheduleIs;
    }

    public static boolean isSet(Note note) {
        List<String> scheduleIds = getScheduleIds(note);
        for (String scheduleId : scheduleIds) {
            if (isExist(scheduleId)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isExist(String scheduleId) {
        Context context = MyApplication.getInstance();
        Intent it = new Intent(context, ScheduleReceiver.class);
        it.setData(Uri.parse(ID_PREFIX + scheduleId));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_EXACT_REQUEST_CODE, it, PendingIntent.FLAG_NO_CREATE);
        return pendingIntent != null;
    }

}
