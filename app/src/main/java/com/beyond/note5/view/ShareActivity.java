package com.beyond.note5.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import com.beyond.note5.bean.Note;
import com.beyond.note5.module.DaggerNoteComponent;
import com.beyond.note5.module.NoteComponent;
import com.beyond.note5.module.NoteModule;
import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.utils.IDUtil;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by beyond on 17-12-15.
 */

public class ShareActivity extends Activity implements NoteView {
    public final static String SEND = "android.intent.action.SEND";
    public final static String PROCESS_TEXT = "android.intent.action.PROCESS_TEXT";

    private Handler handler = new Handler();

    @Inject
    NotePresenter notePresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initInjection();

        final Intent intent = getIntent();
        if ("text/plain".equals(intent.getType()) && (SEND.equals(intent.getAction()) )){
            Note note = generateNoteFromSend(intent);
            notePresenter.addNote(note);
        }
        if ("text/plain".equals(intent.getType()) && PROCESS_TEXT.equals(intent.getAction())){
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Note note = generateNoteFromProcessText(intent);
                notePresenter.addNote(note);
            }
        }

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
        note.setTitle(title);
        note.setContent(content);
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

    @Override
    public void onAddNoteSuccess(Note note) {
        msg("添加成功");
    }

    @Override
    public void msg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFindAllNoteSuccess(List<Note> allNote) {

    }

    @Override
    public void deleteNoteFail(Note note) {

    }

    @Override
    public void deleteNoteSuccess(Note note) {

    }

    @Override
    public void updateNoteSuccess(Note note) {

    }

    @Override
    public void updateNoteFail(Note note) {

    }
}
