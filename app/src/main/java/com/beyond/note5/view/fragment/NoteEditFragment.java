package com.beyond.note5.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.beyond.note5.bean.Note;
import com.beyond.note5.constant.DocumentConst;
import com.beyond.note5.event.AddNoteSuccessEvent;
import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.presenter.NotePresenterImpl;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.view.adapter.view.NoteViewAdapter;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;

/**
 * @author: beyond
 * @date: 2019/1/31
 */

public class NoteEditFragment extends AbstractDocumentEditFragment<Note> {

    protected NotePresenter notePresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initInjection();
    }

    private void initInjection() {
        notePresenter = new NotePresenterImpl(new MyNoteView());
    }

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
        notePresenter.add(createdDocument);
    }

    private class MyNoteView extends NoteViewAdapter {
        @Override
        public void onAddSuccess(Note document) {
            EventBus.getDefault().post(new AddNoteSuccessEvent(document));
        }

        @Override
        public void onAddFail(Note document) {
            ToastUtil.toast(getActivity(),"添加失敗");
        }
    }
}
