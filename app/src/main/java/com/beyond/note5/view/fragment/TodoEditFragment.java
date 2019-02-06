package com.beyond.note5.view.fragment;

import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.AddTodoEvent;
import com.beyond.note5.utils.IDUtil;

import java.util.Date;

/**
 * @author: beyond
 * @date: 2019/1/31
 */

public class TodoEditFragment extends AbstractDocumentEditFragment<Todo> {

    @Override
    protected void sendEventsOnOKClick(String content) {
        Todo todo = new Todo();
        todo.setId(IDUtil.uuid());
        todo.setTitle(content.length()>10?content.substring(1,10):content);
        todo.setContent(content);
        todo.setCreateTime(new Date());
        todo.setLastModifyTime(new Date());
        post( new AddTodoEvent(todo));
    }
}
