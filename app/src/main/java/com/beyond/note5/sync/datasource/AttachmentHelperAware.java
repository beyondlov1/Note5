package com.beyond.note5.sync.datasource;

import com.beyond.note5.sync.datasource.attachment.AttachmentHelper;

/**
 * @author: beyond
 * @date: 2019/8/29
 */

public interface AttachmentHelperAware {
    void setAttachmentHelper(AttachmentHelper helper);
}
