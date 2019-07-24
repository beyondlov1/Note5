package com.beyond.note5.service.schedule.callback;

import android.content.Context;
import android.content.Intent;

public interface ScheduleCallback {
    void onCall(Context context, Intent intent) throws ClassNotFoundException;
}