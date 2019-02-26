package com.beyond.note5.view.fragment;

import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.FillTodoModifyEvent;
import com.beyond.note5.event.UpdateTodoEvent;

import org.apache.commons.lang3.ObjectUtils;
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
        createdDocument.setContent(content);
        createdDocument.setLastModifyTime(new Date());
        createdDocument.setVersion(createdDocument.getVersion() == null?0:createdDocument.getVersion()+1);
        EventBus.getDefault().post(new UpdateTodoEvent(createdDocument));
    }
}
