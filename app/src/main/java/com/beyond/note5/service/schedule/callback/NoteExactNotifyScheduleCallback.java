package com.beyond.note5.service.schedule.callback;

import android.content.Context;
import android.content.Intent;

import com.beyond.note5.bean.Note;
import com.beyond.note5.service.schedule.ScheduleReceiver;
import com.beyond.note5.utils.PreferenceUtil;

import java.util.HashMap;
import java.util.Map;

import static com.beyond.note5.MyApplication.NOTE_NOTIFICATION_SHOULD_SCHEDULE;

/**
 * @author: beyond
 * @date: 2019/7/24
 */

public class NoteExactNotifyScheduleCallback extends NoteNotifyScheduleCallback {

    @Override
    public void onCall(Context context, Intent intent) throws ClassNotFoundException {
        boolean shouldSchedule = PreferenceUtil.getBoolean(NOTE_NOTIFICATION_SHOULD_SCHEDULE, false);
        if (!shouldSchedule){
            return;
        }
        String id = intent.getStringExtra("id");
        Note note = noteModel.findById(id);
        buildNotification(context, note);
    }

    private void scheduleNext(Context context, Intent intent, String id) {
        String lastIndexString = intent.getStringExtra("index");
        if (lastIndexString != null) {
            int lastIndex = Integer.valueOf(lastIndexString);
            int index = lastIndex + 1;
            if (index >= NOTIFICATION_POINTS.length) {
                return;
            }
            String indexString = String.valueOf(index);
            Map<String, String> data = new HashMap<>(2);
            data.put("id", id);
            data.put("index", indexString);
            long startTime = intent.getLongExtra("_startTime", 0L);
            int requestCode = intent.getIntExtra("_requestCode", 0);
            String callbackClass = intent.getStringExtra("_callbackClass");
            startTime += NOTIFICATION_POINTS[index] * 60 * 1000;
            ScheduleReceiver.scheduleOnce(context, requestCode, startTime, callbackClass, data, id);
        }
    }
}
