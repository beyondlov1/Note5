package com.beyond.note5.sync.datasource.dav;

import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.datasource.AttachmentHelperAware;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.attachment.AttachmentHelper;
import com.beyond.note5.sync.datasource.entity.SyncStamp;
import com.beyond.note5.utils.OkWebDavUtil;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author: beyond
 * @date: 2019/8/28
 */

public class NoteMultiDavDataSource extends DefaultMultiDavDataSource<Note> implements AttachmentHelperAware {

    private AttachmentHelper attachmentHelper;

    public NoteMultiDavDataSource(DavDataSourceProperty property, Class<Note> clazz, ExecutorService executorService) {
        super(property, clazz, executorService);
    }

    @Override
    protected void add(Note note) throws IOException {
        super.add(note);

        if (attachmentHelper != null) {
            for (Attachment attachment : note.getAttachments()) {
                attachmentHelper.upload(attachment, this);
            }
        }
    }

    @Override
    public void upload(String id, String name, String localPath) throws IOException {
        Note note = new Note();
        note.setId(id);
        getClient().upload(localPath, OkWebDavUtil.concat(getServer(), getRemotePath(note, name)));
    }

    @Override
    public void download(String id, String name, String localPath) throws IOException {
        Note note = new Note();
        note.setId(id);
        getClient().download(OkWebDavUtil.concat(getServer(), getRemotePath(note, name)), localPath);
    }

    private String getRemotePath(Note note, String fileName) {
        return OkWebDavUtil.concat(
                getPath(note),
                property.getFilesDir(),
                fileName
        );
    }

    @Override
    public List<Note> getChangedData(SyncStamp syncStamp, DataSource<Note> targetDataSource) throws IOException {
        List<Note> changedData = super.getChangedData(syncStamp, targetDataSource);
        for (Note note : changedData) {
            attachmentHelper.add(this, note);
        }
        return changedData;
    }

    @Override
    public void setAttachmentHelper(AttachmentHelper attachmentHelper) {
        this.attachmentHelper = attachmentHelper;
    }

}
