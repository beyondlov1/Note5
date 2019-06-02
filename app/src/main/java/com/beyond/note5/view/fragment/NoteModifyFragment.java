package com.beyond.note5.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.beyond.note5.bean.Note;
import com.beyond.note5.event.FillNoteModifyEvent;
import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.presenter.NotePresenterImpl;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.utils.WebViewUtil;
import com.beyond.note5.view.adapter.view.NoteViewAdapter;

import org.apache.commons.lang3.ObjectUtils;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;

/**
 * @author: beyond
 * @date: 2019/2/5
 */

public class NoteModifyFragment extends AbstractDocumentEditFragment<Note> {

    protected NotePresenter notePresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initInjection();
    }

    private void initInjection() {
        notePresenter = new NotePresenterImpl(new MyNoteView());
    }

    //回显
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void onEventMainThread(FillNoteModifyEvent fillNoteModifyEvent){
        Note note = fillNoteModifyEvent.get();
        createdDocument = ObjectUtils.clone(note);
        editorContent.setText(note.getContent());
        editorContent.setSelection(note.getContent().length());
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
        notePresenter.update(createdDocument);
    }

    private class MyNoteView extends NoteViewAdapter {
        @Override
        public void onUpdateFail(Note document) {
            ToastUtil.toast(getActivity(),"更新失敗");
        }
    }
}
