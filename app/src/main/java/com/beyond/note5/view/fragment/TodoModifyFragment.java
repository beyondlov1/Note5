package com.beyond.note5.view.fragment;

import android.view.View;

import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.DeleteTodoEvent;
import com.beyond.note5.event.FillTodoModifyEvent;
import com.beyond.note5.event.UpdateTodoEvent;
import com.beyond.note5.view.custom.DialogButton;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;

public class TodoModifyFragment extends TodoEditFragment {
    //回显
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void onEventMainThread(FillTodoModifyEvent fillTodoModifyEvent){
        Todo todo = fillTodoModifyEvent.get();
        createdDocument = ObjectUtils.clone(todo);
        contentEditText.setText(todo.getContent());
        contentEditText.setSelection(todo.getContent().length());
    }


    @Override
    protected void sendEventsOnOKClick(String content) {
        if (StringUtils.isBlank(content)){
            EventBus.getDefault().post(new DeleteTodoEvent(createdDocument));
        }else {
            createdDocument.setContent(content);
            createdDocument.setLastModifyTime(new Date());
            createdDocument.setVersion(createdDocument.getVersion() == null?0:createdDocument.getVersion()+1);
            EventBus.getDefault().post(new UpdateTodoEvent(createdDocument));
        }
    }

    @Override
    protected DialogButton initNeutralButton() {
        return new DialogButton("Clear", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentEditText.setText(null);
                createdDocument.setContent("");
            }
        });
    }
}
