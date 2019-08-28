package com.beyond.note5.sync.datasource.attachment;

import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.datasource.FileStore;

/**
 * @author: beyond
 * @date: 2019/8/28
 */

public class AttachmentSource {
    private FileStore fileStore;
    private Note note;
    private Attachment attachment;

    public AttachmentSource(FileStore fileStore, Note note, Attachment attachment) {
        this.fileStore = fileStore;
        this.note = note;
        this.attachment = attachment;
    }

    public FileStore getFileStore() {
        return fileStore;
    }

    public void setFileStore(FileStore fileStore) {
        this.fileStore = fileStore;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }
}
