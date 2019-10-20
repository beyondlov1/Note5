package com.beyond.note5.view.fragment;

import android.view.View;

import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.FillTodoModifyEvent;
import com.beyond.note5.event.ShowTodoEditorEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class TodoSearchResultFragment extends SearchResultFragment<Todo> {

    @Override
    protected void initViewHolder(List<Todo> data, int position, MyViewHolder holder) {
        super.initViewHolder(data, position, holder);
        holder.titleTextView.setVisibility(View.GONE);
    }

    @Override
    public View.OnClickListener getOnItemClickListener(List<Todo> data, int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Todo todo = data.get(position);
                ShowTodoEditorEvent showTodoEditorEvent = new ShowTodoEditorEvent(v);
                EventBus.getDefault().post(showTodoEditorEvent);
                FillTodoModifyEvent event = new FillTodoModifyEvent(todo);
                event.setIndex(position);
                EventBus.getDefault().post(event);
            }
        };
    }
}
