package com.beyond.note5.view;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Note;
import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.presenter.NotePresenterImpl;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.PhotoUtil;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.view.adapter.view.NoteViewAdapter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * @author: beyond
 * @date: 17-12-15
 */

public class ShareActivity extends Activity {
    public final static String SEND = "android.intent.action.SEND";
    public final static String PROCESS_TEXT = "android.intent.action.PROCESS_TEXT";

    private final static String TAG = "ShareActivity";

    private Intent intent;

    @Inject
    NotePresenter notePresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initInjection();

        intent = getIntent();
        if ("text/plain".equals(intent.getType()) && (SEND.equals(intent.getAction()))) {
            Note note = generateNoteFromSend(intent);
            notePresenter.add(note);
        }
        if ("text/plain".equals(intent.getType()) && PROCESS_TEXT.equals(intent.getAction())) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Note note = generateNoteFromProcessText(intent);
                notePresenter.add(note);
            }
        }
        if (intent.getType().length() > 6 && intent.getType().startsWith("image/")) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Note note = generateNoteFromImage(intent);
                if (note != null) {
                    notePresenter.add(note);
                }
            }
        }

        finish();
    }

    private void initInjection() {
        notePresenter = new NotePresenterImpl(new MyNoteView());
    }

    private Note generateNoteFromSend(Intent intent) {
        String title = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        String content = intent.getStringExtra(Intent.EXTRA_TEXT);
        Note note = Note.newInstance();
        note.setTitle(title);
        if (content.startsWith("null ")){
            content = content.substring(5);
        }
        note.setContent(content);
        return note;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private Note generateNoteFromProcessText(Intent intent) {
        String content = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
        Note note = Note.newInstance();
        note.setContent(content);
        return note;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private Note generateNoteFromImage(Intent intent) {
        String[] paths = downloadSharedImages(intent);

        if (paths != null && ArrayUtils.isNotEmpty(paths)) {
            String noteId = IDUtil.uuid();
            StringBuilder content = new StringBuilder();
            List<Attachment> attachments = new ArrayList<>();
            for (String path : paths) {
                if (StringUtils.isNotBlank(path)) {
                    content.append("!file://").append(path).append("\n");
                    Attachment attachment = new Attachment();
                    attachment.setId(IDUtil.uuid());
                    attachment.setNoteId(noteId);
                    attachment.setName(new File(path).getName());
                    attachment.setPath(path);
                    attachments.add(attachment);

                }
            }

            if (StringUtils.isBlank(content.toString())) {
                return null;
            }

            Note note = Note.newInstance();
            note.setId(noteId);
            note.setContent(content.toString());
            note.setAttachments(attachments);
            return note;
        } else {
            return null;
        }

    }

    private String[] downloadSharedImages(Intent intent) {

        ClipData data = intent.getClipData();
        String type = intent.getType();
        String suffix = StringUtils.substring(type, 6);
        if (data == null) {
            return null;
        }
        int itemCount = data.getItemCount();
        String[] downloadPaths = new String[itemCount];

        for (int i = 0; i < itemCount; i++) {
            /*将content：//中的图片下载到指定位置*/
            Uri uri = data.getItemAt(i).getUri();
            File imageFile = PhotoUtil.createImageFile(this, suffix);
            String downloadPath = null;
            try (InputStream inputStream = this.getContentResolver().openInputStream(uri);
                 OutputStream outputStream = new FileOutputStream(imageFile)) {
                if (inputStream != null) {
                    IOUtils.copy(inputStream, outputStream);
                    downloadPath = imageFile.getAbsolutePath();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("ShareActivity", e.getMessage());
            }

            downloadPaths[i] = downloadPath;
        }
        return downloadPaths;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Note note = generateNoteFromImage(intent);
                if (note != null) {
                    notePresenter.add(note);
                }
            }
            finish();
        }
    }

    private class MyNoteView extends NoteViewAdapter {
        @Override
        public void onAddSuccess(Note document) {
            super.onAddSuccess(document);
            ToastUtil.toast(getBaseContext(),"添加成功");
        }
    }
}
