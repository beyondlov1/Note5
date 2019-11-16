package com.beyond.note5.sync.datasource.sql;


import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.datasource.AttachmentHelperAware;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.FileStore;
import com.beyond.note5.sync.datasource.MultiDataSource;
import com.beyond.note5.sync.datasource.attachment.AttachmentHelper;
import com.beyond.note5.sync.datasource.entity.SyncStamp;

import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NoteMultiSqlDataSource extends NoteSqlDataSource implements MultiDataSource<Note>,FileStore,AttachmentHelperAware {

    private String chosenKey;

    private Map<String, SyncStamp> syncStamps = new LinkedHashMap<>();

    private AttachmentHelper attachmentHelper;

    @Override
    public Class<Note> clazz() {
        return Note.class;
    }

    @Override
    public void setChosenKey(String key) {
        chosenKey = key;
    }

    @Override
    public String getChosenKey() {
        return chosenKey;
    }

    @Override
    public void initLastSyncStamps() throws IOException {
        syncStamps = baseSyncStampModel.findAllConnectMe();
    }

    @Override
    public Map<String, SyncStamp> getSyncStampsCache() {
        return syncStamps;
    }

    @Override
    public void setSyncStampsCache(Map<String, SyncStamp> syncStamps) {
        this.syncStamps = syncStamps;
    }

    @Override
    protected void addAll(List<Note> addList, String... oppositeKeys) throws IOException {
        super.addAll(addList, oppositeKeys);

        for (Note note : addList) {
            List<Attachment> attachments = note.getAttachments();
            if (CollectionUtils.isEmpty(attachments)){
                continue;
            }
            for (Attachment attachment : attachments) {
                attachmentHelper.saveAttachment(attachment,this);
            }
        }
    }

    @Override
    public void upload(String id, String name, String localPath) throws IOException {
        // do nothing
    }

    @Override
    public void download(String id, String name, String localPath) throws IOException {
        // do nothing
    }

    @Override
    public List<Note> getChangedData(SyncStamp syncStamp, DataSource<Note> targetDataSource) throws IOException {
        List<Note> changedData = super.getChangedData(syncStamp, targetDataSource);
        for (Note note : changedData) {
            attachmentHelper.joinPool(this, note);
        }
        return changedData;
    }

    @Override
    public void setAttachmentHelper(AttachmentHelper helper) {
        this.attachmentHelper = helper;
    }
}
