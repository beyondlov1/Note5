package com.beyond.note5.view.fragment;

import android.content.DialogInterface;
import android.os.Bundle;

import com.beyond.note5.bean.Note;
import com.beyond.note5.event.FillNoteModifyEvent;
import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.presenter.NotePresenterImpl;
import com.beyond.note5.utils.StatusBarUtil;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.utils.WebViewUtil;
import com.beyond.note5.view.NoteView;
import com.beyond.note5.view.adapter.view.NoteViewAdapter;

import org.apache.commons.lang3.ObjectUtils;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.greendao.annotation.NotNull;

import java.util.Date;

/**
 * @author: beyond
 * @date: 2019/2/5
 */

public class NoteModifyFragment extends AbstractNoteEditorFragment {

    protected NotePresenter notePresenter;

    private NoteView noteView = new MyNoteView();

    @Override
    protected void init(Bundle savedInstanceState) {
        notePresenter = new NotePresenterImpl(noteView);
    }

    //回显
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void onEventMainThread(FillNoteModifyEvent fillNoteModifyEvent){
        Note note = fillNoteModifyEvent.get();
        creatingDocument = ObjectUtils.clone(note);
        editorContent.setText(note.getContent());
        editorContent.setSelection(note.getContent().length());
        WebViewUtil.loadWebContent(displayWebView,note);
    }

    @Override
    protected Note creatingDocument() {
        //do nothing
        return null;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        StatusBarUtil.hideStatusBar(getActivity());
    }

    @Override
    public void saveInternal(@NotNull CharSequence cs) {
        creatingDocument.setContent(cs.toString());
        creatingDocument.setLastModifyTime(new Date());
        creatingDocument.setVersion(creatingDocument.getVersion() == null?0: creatingDocument.getVersion()+1);
        notePresenter.update(creatingDocument);
    }

    private class MyNoteView extends NoteViewAdapter {
        @Override
        public void onUpdateFail(Note document) {
            ToastUtil.toast(getActivity(),"更新失敗");
        }
    }
}
