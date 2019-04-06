package com.beyond.note5.view.fragment;

import com.beyond.note5.bean.Note;
import com.beyond.note5.event.FillNoteModifyEvent;
import com.beyond.note5.event.UpdateNoteEvent;
import com.beyond.note5.utils.WebViewUtil;

import org.apache.commons.lang3.ObjectUtils;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;

/**
 * @author: beyond
 * @date: 2019/2/5
 */

public class NoteModifyFragment extends AbstractDocumentEditFragment<Note> {

    //回显
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void onEventMainThread(FillNoteModifyEvent fillNoteModifyEvent){
        Note note = fillNoteModifyEvent.get();
        createdDocument = ObjectUtils.clone(note);
        contentEditText.setText(note.getContent());
        contentEditText.setSelection(note.getContent().length());
        WebViewUtil.loadWebContent(displayWebView,note);
    }

    @Override
    protected Note initCreatedDocument() {
        //do nothing
        return null;
    }

    @Override
    protected void sendEventsOnOKClick(String content) {
        createdDocument.setContent(content);
        createdDocument.setLastModifyTime(new Date());
        createdDocument.setVersion(createdDocument.getVersion() == null?0:createdDocument.getVersion()+1);
        post(new UpdateNoteEvent(createdDocument));
//        post(new ModifyNoteDoneEvent(createdDocument)); //把新增和修改改成异步就不能这么用了， see： NoteListFragment:onUpdateSuccess
    }
}
