package com.beyond.note5.view;

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
import com.beyond.note5.event.RefreshNoteListEvent;
import com.beyond.note5.module.DaggerNoteComponent;
import com.beyond.note5.module.NoteComponent;
import com.beyond.note5.module.NoteModule;
import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.PhotoUtil;
import com.beyond.note5.view.adapter.AbstractActivityNoteView;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

/**
 * @author: beyond
 * @date: 17-12-15
 */

public class ShareActivity extends AbstractActivityNoteView {
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
                //如果这里需要权限可以这样写， 记得要加finish()!!!
//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                    PermissionsUtil.requestPermission(this, new PermissionListener() {
//                        @Override
//                        public void permissionGranted(@NonNull String[] permission) {
//                            Note note = generateNoteFromImage(intent);
//                            if (note != null) {
//                                notePresenter.add(note);
//                            }
//                            EventBus.getDefault().post(new RefreshNoteListEvent(TAG));
//                            finish();
//                        }
//
//                        @Override
//                        public void permissionDenied(@NonNull String[] permission) {
//
//                        }
//                    }, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, false, null);
//                    finish();
//                    return;
//                }
                Note note = generateNoteFromImage(intent);
                if (note != null) {
                    notePresenter.add(note);
                }
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
        if (StringUtils.isNotBlank(title) && !StringUtils.equalsIgnoreCase(StringUtils.trim(title), "null")) {
            note.setContent(String.format("### %s\n%s", title, content));
        } else {
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

            Date currDate = new Date();
            Note note = new Note();
            note.setId(noteId);
            note.setContent(content.toString());
            note.setAttachments(attachments);
            note.setCreateTime(currDate);
            note.setLastModifyTime(currDate);
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
            EventBus.getDefault().post(new RefreshNoteListEvent(TAG));
            finish();
        }
    }
}
