package com.beyond.note5.view;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import com.beyond.note5.bean.Note;
import com.beyond.note5.event.RefreshNoteListEvent;
import com.beyond.note5.module.DaggerNoteComponent;
import com.beyond.note5.module.NoteComponent;
import com.beyond.note5.module.NoteModule;
import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.view.adapter.AbstractActivityNoteView;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.Date;

import javax.inject.Inject;

/**
 * @author: beyond
 * @date: 17-12-15
 */

public class ShareActivity extends AbstractActivityNoteView {
    public final static String SEND = "android.intent.action.SEND";
    public final static String PROCESS_TEXT = "android.intent.action.PROCESS_TEXT";

    private final static String TAG = "ShareActivity";

    @Inject
    NotePresenter notePresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initInjection();

        final Intent intent = getIntent();
        if ("text/plain".equals(intent.getType()) && (SEND.equals(intent.getAction()) )){
            Note note = generateNoteFromSend(intent);
            notePresenter.add(note);
        }
        if ("text/plain".equals(intent.getType()) && PROCESS_TEXT.equals(intent.getAction())){
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Note note = generateNoteFromProcessText(intent);
                notePresenter.add(note);
            }
        }

        EventBus.getDefault().post(new RefreshNoteListEvent(TAG));

        finish();
    }

    private void initInjection() {
        NoteComponent noteComponent = DaggerNoteComponent.builder().noteModule(new NoteModule(this)).build();
        noteComponent.inject(this);
    }

    private Note generateNoteFromSend(Intent intent) {
        String title = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        String content = intent.getStringExtra(Intent.EXTRA_TEXT);
        Date currDate = new Date();
        Note note = new Note();
        note.setId(IDUtil.uuid());
        if (StringUtils.isNotBlank(title)){
            note.setContent(String.format("### %s\n%s",title,content));
        }else {
            note.setContent(content);
        }
        note.setCreateTime(currDate);
        note.setLastModifyTime(currDate);
        return note;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private Note generateNoteFromProcessText(Intent intent) {
        String content = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
        Date currDate = new Date();
        Note note = new Note();
        note.setId(IDUtil.uuid());
        note.setContent(content);
        note.setCreateTime(currDate);
        note.setLastModifyTime(currDate);
        return note;
    }

}
