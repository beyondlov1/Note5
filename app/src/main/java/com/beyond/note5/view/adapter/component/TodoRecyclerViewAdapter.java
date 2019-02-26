package com.beyond.note5.view.adapter.component;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.beyond.note5.R;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.CompleteTodoEvent;
import com.beyond.note5.event.FillTodoModifyEvent;
import com.beyond.note5.view.adapter.component.header.Header;
import com.beyond.note5.view.adapter.component.header.ItemDataGenerator;
import com.beyond.note5.view.adapter.component.viewholder.TodoViewHolder;
import com.beyond.note5.view.fragment.TodoModifyFragment;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

public class TodoRecyclerViewAdapter extends DocumentRecyclerViewAdapter<Todo,TodoViewHolder> {

    public TodoRecyclerViewAdapter(Context context, ItemDataGenerator<Todo> itemDataGenerator) {
        super(context, itemDataGenerator);
    }

    @Override
    protected TodoViewHolder getViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    protected void initHeaderDisplay(int position, Header header, TodoViewHolder viewHolder) {
        viewHolder.checkbox.setVisibility(View.GONE);
        viewHolder.title.setVisibility(View.VISIBLE);
        viewHolder.title.setText(header.getContent());
        viewHolder.title.setTextColor(context.getResources().getColor(R.color.dark_yellow));
        viewHolder.content.setVisibility(View.GONE);
        viewHolder.container.setOnClickListener(null);
        viewHolder.dataContainer.setBackground(null);
        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
        layoutParams.setFullSpan(true);
    }

    @Override
    protected void initContentDisplay(TodoViewHolder viewHolder, Todo todo, int position) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(13);
        gradientDrawable.setStroke(1, ContextCompat.getColor(context, R.color.dark_gray));
        viewHolder.dataContainer.setBackground(gradientDrawable);
        viewHolder.checkbox.setVisibility(View.VISIBLE);
        viewHolder.checkbox.setChecked(false); // Todo: 要根据todo的值来显示
        viewHolder.title.setVisibility(View.GONE);
        viewHolder.content.setVisibility(View.VISIBLE);
        viewHolder.content.setTextSize(12);
        viewHolder.content.setText(StringUtils.trim(todo.getContent()));
    }

    @Override
    protected void initContentEvent(TodoViewHolder viewHolder, final Todo todo) {
        viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    EventBus.getDefault().post(new CompleteTodoEvent(todo));
                }
            }
        });

        viewHolder.content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showModifyView(todo);
            }
        });
    }

    private void showModifyView(Todo todo) {
        TodoModifyFragment todoModifyFragment = new TodoModifyFragment();
        FragmentActivity activity = (FragmentActivity) context;
        todoModifyFragment.show(activity.getSupportFragmentManager(), "modifyDialog");
        EventBus.getDefault().postSticky(new FillTodoModifyEvent(todo));
    }

}
