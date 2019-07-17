package com.beyond.note5.event;

import com.beyond.note5.bean.Todo;

import java.util.List;

/**
 * @author: beyond
 * @date: 2019/7/17
 */

public class UpdateTodoAllSuccessEvent extends AbstractEvent<List<Todo>>{
    public UpdateTodoAllSuccessEvent(List<Todo> todos) {
        super(todos);
    }
}
