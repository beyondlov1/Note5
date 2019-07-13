package com.beyond.note5.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.beyond.note5.bean.Note;
import com.beyond.note5.event.ScrollNoteToTopEvent;
import com.beyond.note5.view.MainActivity;

import org.greenrobot.eventbus.EventBus;

public class NotificationClickReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        intent.setClass(context,MainActivity.class);
        context.startActivity(intent);
        Note note = new Note();
        note.setId(intent.getStringExtra("id"));
        EventBus.getDefault().post(new ScrollNoteToTopEvent(note));
    }
}