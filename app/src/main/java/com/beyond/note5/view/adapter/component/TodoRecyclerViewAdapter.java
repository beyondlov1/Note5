package com.beyond.note5.view.adapter.component;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.beyond.note5.MyApplication;
import com.beyond.note5.R;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.constant.DocumentConst;
import com.beyond.note5.event.CompleteTodoEvent;
import com.beyond.note5.event.FillTodoModifyEvent;
import com.beyond.note5.event.RefreshTodoListEvent;
import com.beyond.note5.view.adapter.component.header.Header;
import com.beyond.note5.view.adapter.component.header.ItemDataGenerator;
import com.beyond.note5.view.adapter.component.viewholder.TodoViewHolder;
import com.beyond.note5.view.fragment.TodoModifyFragment;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import static com.beyond.note5.model.TodoModelImpl.IS_SHOW_READ_FLAG_DONE;

public class TodoRecyclerViewAdapter extends DocumentRecyclerViewAdapter<Todo, TodoViewHolder> {

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
        viewHolder.checkbox.setChecked(false);
        viewHolder.title.setVisibility(View.VISIBLE);
        viewHolder.title.setText(header.getContent());
        viewHolder.title.setTextColor(context.getResources().getColor(R.color.dark_yellow));
        viewHolder.content.setVisibility(View.GONE);
        viewHolder.content.setPaintFlags(viewHolder.title.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        viewHolder.container.setOnClickListener(null);
        viewHolder.dataContainer.setBackground(null);
        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
        layoutParams.setFullSpan(true);
    }

    @Override
    protected void initHeadEvent(int position, Header header, TodoViewHolder viewHolder) {
        super.initHeadEvent(position, header, viewHolder);
        viewHolder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context.getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                        .getBoolean(IS_SHOW_READ_FLAG_DONE,false)){
                    context.getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                            .putBoolean(IS_SHOW_READ_FLAG_DONE,false).apply();
                }else {
                    context.getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                            .putBoolean(IS_SHOW_READ_FLAG_DONE,true).apply();
                }
                EventBus.getDefault().post(new RefreshTodoListEvent(null));
            }
        });
    }

    @Override
    protected void initContentDisplay(TodoViewHolder viewHolder, Todo todo, int position) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(13);
        gradientDrawable.setStroke(1, ContextCompat.getColor(context, R.color.dark_gray));
        viewHolder.dataContainer.setBackground(gradientDrawable);
        viewHolder.checkbox.setVisibility(View.VISIBLE);
        viewHolder.title.setVisibility(View.GONE);
        if (todo.getReadFlag().equals(DocumentConst.READ_FLAG_DONE)) {
            viewHolder.checkbox.setChecked(true);
            viewHolder.content.setPaintFlags(viewHolder.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            viewHolder.content.setTextColor(context.getResources().getColor(R.color.medium_gray));
            viewHolder.content.setTextSize(8);
        } else {
            viewHolder.checkbox.setChecked(false);
            viewHolder.content.setPaintFlags(viewHolder.title.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            viewHolder.content.setTextColor(context.getResources().getColor(R.color.darker_gray));
            viewHolder.content.setTextSize(12);
        }

        viewHolder.content.setVisibility(View.VISIBLE);
        viewHolder.content.setText(StringUtils.trim(todo.getContent()));
    }

    @Override
    protected void initContentEvent(final TodoViewHolder viewHolder, final Todo todo) {
        viewHolder.checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    todo.setReadFlag(DocumentConst.READ_FLAG_DONE);
                    EventBus.getDefault().post(new CompleteTodoEvent(todo));
                } else {
                    todo.setReadFlag(DocumentConst.READ_FLAG_NORMAL);
                    EventBus.getDefault().post(new CompleteTodoEvent(todo));
                }
            }
        });

        viewHolder.dataContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ShowTodoEditEvent showTodoEditEvent = new ShowTodoEditEvent(v);
//                EventBus.getDefault().post(showTodoEditEvent);
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
