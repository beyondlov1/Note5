package com.beyond.note5.view.fragment;

import com.beyond.note5.bean.Note;
import com.beyond.note5.event.AddNoteEvent;
import com.beyond.note5.utils.IDUtil;

import java.util.Date;

/**
 * Created by beyond on 2019/1/31.
 */

public class NoteEditFragment extends AbstractDocumentEditFragment<Note> {

    public NoteEditFragment() {
        createdDocument = new Note();
    }

    @Override
    protected void sendEventsOnOKClick(String content) {
        createdDocument.setId(IDUtil.uuid());
        createdDocument.setContent(content);
        createdDocument.setCreateTime(new Date());
        createdDocument.setLastModifyTime(new Date());
        post(new AddNoteEvent(createdDocument));
    }
}
