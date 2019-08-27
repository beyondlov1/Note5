package com.beyond.note5.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.beyond.note5.R;
import com.beyond.note5.bean.Document;
import com.beyond.note5.bean.Note;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.constant.DocumentConst;
import com.beyond.note5.event.AfterFloatEditorSavedEvent;
import com.beyond.note5.event.RefreshNoteListEvent;
import com.beyond.note5.event.RefreshTodoListEvent;
import com.beyond.note5.presenter.DocumentCompositePresenter;
import com.beyond.note5.presenter.DocumentCompositePresenterImpl;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.InputMethodUtil;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.view.CalendarView;
import com.beyond.note5.view.DocumentCompositeView;
import com.beyond.note5.view.NoteView;
import com.beyond.note5.view.PredictView;
import com.beyond.note5.view.TodoView;
import com.beyond.note5.view.adapter.view.NoteViewAdapter;
import com.beyond.note5.view.adapter.view.TodoViewAdapter;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;

import javax.inject.Inject;

public class FloatEditorFragment extends Fragment implements View.OnClickListener, DocumentCompositeView {

    private static final String TAG = "FloatEditorFragment";

    private View viewGroup;
    private EditText editText;
    private ImageButton saveButton;

    @Inject
    DocumentCompositePresenter presenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initInjection();
    }

    private void initInjection() {
        presenter = new DocumentCompositePresenterImpl(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = inflater.inflate(R.layout.fragment_float_editor, container, false);
        initView();
        initEvent();
        return viewGroup;
    }


    private void initView() {
        editText = viewGroup.findViewById(R.id.float_editor_fragment_edit_text);
        saveButton = viewGroup.findViewById(R.id.float_editor_fragment_save_button);

        InputMethodUtil.showKeyboard(editText);
    }

    private void initEvent() {
        saveButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {

        if (v == saveButton) {
            save();
        }
    }

    private void save() {

        String content = editText.getText().toString();
        Document document = new Document();
        document.setId(IDUtil.uuid());
        document.setContent(content);
        document.setCreateTime(new Date());
        document.setLastModifyTime(new Date());
        document.setVersion(1);
        document.setReadFlag(DocumentConst.READ_FLAG_NORMAL);
        presenter.add(document);

    }

    @Override
    public NoteView getNoteView() {
        return new NoteViewAdapter(){
            @Override
            public void onAddSuccess(Note document) {
                ToastUtil.toast(getActivity(), "添加记录成功");
                EventBus.getDefault().post(new RefreshNoteListEvent(null));
                EventBus.getDefault().post(new AfterFloatEditorSavedEvent(document));
            }

            @Override
            public void onAddFail(Note document) {
                ToastUtil.toast(getContext(), "添加失败");
            }
        };
    }

    @Override
    public TodoView getTodoView() {
        return new TodoViewAdapter(){
            @Override
            public void onAddSuccess(Todo document) {
                ToastUtil.toast(getActivity(), "添加待办事项成功");
                EventBus.getDefault().post(new RefreshTodoListEvent(null));
                EventBus.getDefault().post(new AfterFloatEditorSavedEvent(document));
            }

            @Override
            public void onAddFail(Todo document) {
                ToastUtil.toast(getContext(), "添加失败");
            }
        };
    }

    @Override
    public CalendarView getCalendarView() {
        return null;
    }

    @Override
    public PredictView getPredictView() {
        return null;
    }

}
