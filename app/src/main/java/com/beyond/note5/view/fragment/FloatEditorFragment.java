package com.beyond.note5.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.beyond.note5.R;
import com.beyond.note5.bean.Note;
import com.beyond.note5.constant.DocumentConst;
import com.beyond.note5.event.AfterFloatEditorSavedEvent;
import com.beyond.note5.event.CompleteTodoEvent;
import com.beyond.note5.event.RefreshNoteListEvent;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.InputMethodUtil;
import com.beyond.note5.view.adapter.AbstractNoteFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;

public class FloatEditorFragment extends AbstractNoteFragment implements View.OnClickListener {

    private static final String TAG = "FloatEditorFragment";

    private EditText editText;
    private ImageButton saveButton;

    @Override
    protected ViewGroup initViewGroup(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.fragment_float_editor,container,false);
    }

    @Override
    protected void initView() {
        editText = viewGroup.findViewById(R.id.float_editor_fragment_edit_text);
        saveButton = viewGroup.findViewById(R.id.float_editor_fragment_save_button);

        InputMethodUtil.showKeyboard(editText);
    }

    @Override
    protected void initEvent() {
        saveButton.setOnClickListener(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void test(CompleteTodoEvent event){

    }

    @Override
    public void onClick(View v) {

        if (v == saveButton){
            saveNote();
        }
    }

    private void saveNote() {
        String content = editText.getText().toString();
        Note note = new Note();
        note.setId(IDUtil.uuid());
        note.setContent(content);
        note.setCreateTime(new Date());
        note.setLastModifyTime(new Date());
        note.setVersion(1);
        note.setReadFlag(DocumentConst.READ_FLAG_NORMAL);
        notePresenter.add(note);
        post(new RefreshNoteListEvent(TAG));
        post(new AfterFloatEditorSavedEvent(note));
    }
}
