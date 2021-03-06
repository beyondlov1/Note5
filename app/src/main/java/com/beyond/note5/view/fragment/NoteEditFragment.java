package com.beyond.note5.view.fragment;

import android.os.Bundle;

import com.beyond.note5.bean.Note;
import com.beyond.note5.constant.DocumentConst;
import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.presenter.NotePresenterImpl;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.view.NoteView;
import com.beyond.note5.view.adapter.view.NoteViewAdapter;

import org.greenrobot.greendao.annotation.NotNull;

import java.util.Date;

/**
 * @author: beyond
 * @date: 2019/1/31
 */

public class NoteEditFragment extends AbstractNoteEditorFragment {

    protected NotePresenter notePresenter;

    private NoteView noteView = new MyNoteView();

    @Override
    protected void init(Bundle savedInstanceState) {
        notePresenter = new NotePresenterImpl(noteView);
    }

    @Override
    protected Note creatingDocument() {
        return new Note();
    }

    @Override
    public void saveInternal(@NotNull CharSequence cs) {
        creatingDocument.setId(IDUtil.uuid());
        creatingDocument.setContent(cs.toString());
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
