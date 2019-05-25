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
import com.beyond.note5.module.DaggerDocumentCompositeComponent;
import com.beyond.note5.module.DocumentCompositeComponent;
import com.beyond.note5.module.DocumentCompositeModule;
import com.beyond.note5.presenter.DocumentCompositePresenter;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.InputMethodUtil;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.view.DocumentCompositeView;

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
        DocumentCompositeComponent documentCompositeComponent = DaggerDocumentCompositeComponent.builder()
                .documentCompositeModule(new DocumentCompositeModule(this)).build();
        documentCompositeComponent.inject(this);
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
    public void onNoteAddSuccess(Note document) {

        EventBus.getDefault().post(new RefreshNoteListEvent(null));
        EventBus.getDefault().post(new AfterFloatEditorSavedEvent(document));
    }

    @Override
    public void onTodoAddSuccess(Todo todo) {
//        if (todo.getReminder()!=null) {
//            calendarPresenter.add(todo);
//        }
//        predictPresenter.train(todo.getContent());
        EventBus.getDefault().post(new RefreshTodoListEvent(null));
        EventBus.getDefault().post(new AfterFloatEditorSavedEvent(todo));
    }

    @Override
    public void onAddFail() {
        ToastUtil.toast(getContext(), "添加失败");
    }
}
