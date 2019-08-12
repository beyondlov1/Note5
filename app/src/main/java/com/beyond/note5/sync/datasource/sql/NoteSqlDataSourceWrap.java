package com.beyond.note5.sync.datasource.sql;

import android.util.Log;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.context.SyncContext;
import com.beyond.note5.sync.context.SyncContextAware;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.dav.DavDataSource;
import com.beyond.note5.utils.OkWebDavUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class NoteSqlDataSourceWrap extends NoteSqlDataSource implements SyncContextAware {

    private SyncContext context;

    @SuppressWarnings("unchecked")
    private DavDataSource<Note> getDavDataSource() {
        DataSource correspondDataSource = context.getOppositeDataSource(this);
        if (correspondDataSource instanceof DavDataSource) {
            return (DavDataSource<Note>) correspondDataSource;
        }
        return null;
    }

    @Override
    public void saveAll(List<Note> notes, String... oppositeKeys) throws IOException {
        super.saveAll(notes, oppositeKeys);

        if (getDavDataSource() == null) {
            return;
        }
        for (Note note : notes) {
            List<Attachment> attachments = note.getAttachments();
            if (attachments != null && !attachments.isEmpty()) {
                for (Attachment attachment : attachments) {
                    try {
                        if (new File(attachment.getPath()).exists()) {
                            continue;
                        }
                        getDavDataSource().download(
                                getRemoteUrl(note, attachment),
                                getLocalPath(attachment)
                        );
                    } catch (IOException e) {
                        Log.e(this.getClass().getSimpleName(), "下载文件失败", e);
                    }
                }
            }
        }
    }


    private String getLocalPath(Attachment attachment) {
        return attachment.getPath();
    }

    private String getRemoteUrl(Note note, Attachment attachment) {
        DavDataSource<Note> davDataSource = getDavDataSource();
        assert davDataSource != null;
        return OkWebDavUtil.concat(
                OkWebDavUtil.concat(davDataSource.getServer(), davDataSource.getPath(note)),
                getLocalPath(attachment).replaceFirst(MyApplication.getInstance().getFileStorageDir().getAbsolutePath(), "/files")
        );
    }

    @Override
    public void setContext(SyncContext context) {
        this.context = context;
    }
}
