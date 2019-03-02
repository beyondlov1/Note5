package com.beyond.note5.view.fragment;

import android.view.View;
import android.view.ViewStub;
import android.widget.ImageButton;

import com.beyond.note5.R;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.DeleteTodoEvent;
import com.beyond.note5.event.FillTodoModifyEvent;
import com.beyond.note5.event.HideTodoEditEvent;
import com.beyond.note5.event.UpdateTodoEvent;
import com.beyond.note5.utils.InputMethodUtil;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;

public class TodoModifySuperFragment extends TodoEditSuperFragment {

    private ImageButton clearButton;
    private ImageButton saveButton;

    //回显
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void onEventMainThread(FillTodoModifyEvent fillTodoModifyEvent){
        Todo todo = fillTodoModifyEvent.get();
        createdDocument = ObjectUtils.clone(todo);
        contentEditText.setText(todo.getContent());
        contentEditText.setSelection(todo.getContent().length());
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        ViewStub toolsContainer = view.findViewById(R.id.fragment_edit_todo_view_stub);
        toolsContainer.inflate();
        clearButton = view.findViewById(R.id.fragment_edit_todo_clear);
        saveButton = view.findViewById(R.id.fragment_edit_todo_save);
    }

    @Override
    protected void initEvent(View view) {
        super.initEvent(view);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentEditText.setText(null);
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = contentEditText.getText().toString();
                if (StringUtils.isBlank(content)){
                    EventBus.getDefault().post(new DeleteTodoEvent(createdDocument));
                }else {
                    createdDocument.setContent(content);
                    createdDocument.setLastModifyTime(new Date());
                    createdDocument.setVersion(createdDocument.getVersion() == null?0:createdDocument.getVersion()+1);
                    EventBus.getDefault().post(new UpdateTodoEvent(createdDocument));
                }
                EventBus.getDefault().post(new HideTodoEditEvent(null));
                InputMethodUtil.hideKeyboard(contentEditText);
            }
        });
    }
}
