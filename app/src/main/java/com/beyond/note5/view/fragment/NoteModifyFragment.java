package com.beyond.note5.view.fragment;

import com.beyond.note5.bean.Note;
import com.beyond.note5.event.Event;
import com.beyond.note5.event.FillNoteModifyEvent;
import com.beyond.note5.event.UpdateNoteEvent;
import com.beyond.note5.utils.WebViewUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;

/**
 * Created by beyond on 2019/2/5.
 */

public class NoteModifyFragment extends AbstractDocumentEditFragment<Note> {

    //回显
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void onEventMainThread(FillNoteModifyEvent fillNoteModifyEvent){
        Note note = fillNoteModifyEvent.get();
        createdDocument = note;
        contentEditText.setText(note.getContent());
        contentEditText.setSelection(note.getContent().length());
        WebViewUtil.loadWebContent(displayWebView,note);
    }

    @Override
    protected Event onPositiveButtonClick(String content) {
        createdDocument.setContent(content);
        createdDocument.setLastModifyTime(new Date());
        return new UpdateNoteEvent(createdDocument);
    }
}
