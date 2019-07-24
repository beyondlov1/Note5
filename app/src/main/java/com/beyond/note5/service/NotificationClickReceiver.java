package com.beyond.note5.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.beyond.note5.bean.Note;
import com.beyond.note5.constant.LoadType;
import com.beyond.note5.event.ScrollNoteToTopEvent;
import com.beyond.note5.event.ShowNoteDetailEvent;
import com.beyond.note5.model.NoteModel;
import com.beyond.note5.model.NoteModelImpl;
import com.beyond.note5.utils.ListUtil;
import com.beyond.note5.view.MainActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class NotificationClickReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        intent.setClass(context,MainActivity.class);
        context.startActivity(intent);
        Note note = new Note();
        note.setId(intent.getStringExtra("id"));
        scrollToTop(note);
        showDetail(note);
    }

    private void scrollToTop(Note note) {
        EventBus.getDefault().post(new ScrollNoteToTopEvent(note));
    }

    private void showDetail(Note note) {
        ShowNoteDetailEvent showNoteDetailEvent = new ShowNoteDetailEvent(null);
        NoteModel noteModel = NoteModelImpl.getSingletonInstance();
        List<Note> all = noteModel.findAll();
        int index = ListUtil.getIndexById(all, note.getId());
        showNoteDetailEvent.setLoadType(LoadType.CONTENT);
        showNoteDetailEvent.setData(all);
        showNoteDetailEvent.setIndex(index);
        EventBus.getDefault().post(showNoteDetailEvent);
    }
}