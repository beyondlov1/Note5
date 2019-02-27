package com.beyond.note5.view.fragment;

import com.beyond.note5.bean.Note;
import com.beyond.note5.constant.DocumentConst;
import com.beyond.note5.event.AddNoteEvent;
import com.beyond.note5.utils.IDUtil;

import java.util.Date;

/**
 * @author: beyond
 * @date: 2019/1/31
 */

public class NoteEditFragment extends AbstractDocumentEditFragment<Note> {

    @Override
    protected Note initCreatedDocument() {
        return new Note();
    }

    @Override
    protected void sendEventsOnOKClick(String content) {
        createdDocument.setId(IDUtil.uuid());
        createdDocument.setContent(content);
        createdDocument.setCreateTime(new Date());
        createdDocument.setLastModifyTime(new Date());
        createdDocument.setVersion(1);
        createdDocument.setReadFlag(DocumentConst.READ_FLAG_NORMAL);
        post(new AddNoteEvent(createdDocument));
    }
}
