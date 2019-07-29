package com.beyond.note5.view.fragment;

import android.os.Bundle;

import com.beyond.note5.bean.Note;
import com.beyond.note5.constant.DocumentConst;
import com.beyond.note5.inject.BeanInjectUtils;
import com.beyond.note5.inject.PrototypeInject;
import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.view.adapter.view.NoteViewAdapter;

import java.util.Date;

/**
 * @author: beyond
 * @date: 2019/1/31
 */

public class NoteEditFragment extends AbstractNoteEditorFragment {

    @PrototypeInject
    protected NotePresenter notePresenter;

    @Override
    protected void init(Bundle savedInstanceState) {
        BeanInjectUtils.inject(this,new MyNoteView());
    }

    @Override
    protected Note creatingDocument() {
        return new Note();
    }

    @Override
    protected void onOKClick() {
        creatingDocument.setId(IDUtil.uuid());
        creatingDocument.setCreateTime(new Date());
        creatingDocument.setLastModifyTime(new Date());
        creatingDocument.setVersion(1);
        creatingDocument.setReadFlag(DocumentConst.READ_FLAG_NORMAL);
        notePresenter.add(creatingDocument);
    }

    private class MyNoteView extends NoteViewAdapter {
        @Override
        public void onAddFail(Note document) {
            ToastUtil.toast(getActivity(),"添加失敗");
        }
    }
}
