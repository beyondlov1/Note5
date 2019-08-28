package com.beyond.note5.sync.datasource.attachment;

import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.datasource.FileStore;
import com.beyond.note5.sync.utils.SyncUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author: beyond
 * @date: 2019/8/28
 */

public class AttachmentHelper {

    private List<AttachmentSource> attachmentSources = new ArrayList<>();

    private Map<String, AttachmentSource> attachmentSourceMap = new HashMap<>();

    private ExecutorService executor;

    public AttachmentHelper() {
    }

    public AttachmentHelper(ExecutorService executor) {
        this.executor = executor;
    }

    public void add(FileStore fileStore, Note note) {
        for (Attachment attachment : note.getAttachments()) {
            if (checkAttachment(attachment)) {
                AttachmentSource a = new AttachmentSource(fileStore, note, attachment);
                attachmentSources.add(a);
                attachmentSourceMap.put(attachment.getId(), a);
            }
        }
    }

    private boolean checkAttachment(Attachment attachment) {
        return attachment != null && StringUtils.isNotBlank(attachment.getPath());
    }

    public void dispatch(FileStore... fileStores) {
        SyncUtils.executor(attachmentSources)
                .executorService(executor)
                .execute(new SyncUtils.ParamCallable<AttachmentSource, Object>() {
                    @Override
                    public Object call(AttachmentSource singleExecutor) throws Exception {
                        singleExecutor.getFileStore().download(singleExecutor.getNote().getId(), singleExecutor.getAttachment().getPath());
                        return null;
                    }
                });
        SyncUtils.executor(Arrays.asList(fileStores))
                .executorService(executor)
                .execute(new SyncUtils.ParamCallable<FileStore, Object>() {
                    @Override
                    public Object call(FileStore singleExecutor) throws Exception {
                        uploadAllAttachment(singleExecutor);
                        return null;
                    }
                });
    }

    private void uploadAllAttachment(FileStore fileStore) {
        SyncUtils.executor(attachmentSources)
                .execute(new SyncUtils.ParamCallable<AttachmentSource, Object>() {
                    @Override
                    public Object call(AttachmentSource attachmentSource) throws Exception {
                        fileStore.upload(attachmentSource.getNote().getId(), attachmentSource.getAttachment().getPath());
                        return null;
                    }
                });
    }

    public void upload(Attachment attachment, FileStore fileStore) throws IOException {
        String id = attachment.getId();
        AttachmentSource attachmentSource = attachmentSourceMap.get(id);
        attachmentSource.getFileStore().download(attachmentSource.getNote().getId(), attachmentSource.getAttachment().getPath());
        fileStore.upload(attachmentSource.getNote().getId(), attachmentSource.getAttachment().getPath());
    }
}
