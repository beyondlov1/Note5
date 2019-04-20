package com.beyond.note5.view.adapter.component;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.SpannableString;
import android.text.Spanned;
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
import com.beyond.note5.event.InCompleteTodoEvent;
import com.beyond.note5.event.RefreshTodoListEvent;
import com.beyond.note5.event.ShowTodoEditEvent;
import com.beyond.note5.event.UpdateTodoPriorityEvent;
import com.beyond.note5.utils.HtmlUtil;
import com.beyond.note5.view.adapter.component.header.Header;
import com.beyond.note5.view.adapter.component.header.ItemDataGenerator;
import com.beyond.note5.view.adapter.component.header.TodoHeader;
import com.beyond.note5.view.adapter.component.viewholder.TodoViewHolder;
import com.time.util.DateUtil;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.Date;

import static com.beyond.note5.model.TodoModelImpl.IS_SHOW_READ_FLAG_DONE;

public class TodoRecyclerViewAdapter extends DocumentRecyclerViewAdapter<Todo, TodoViewHolder> {

    public TodoRecyclerViewAdapter(Context context, ItemDataGenerator<Todo,TodoHeader> itemDataGenerator) {
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
        viewHolder.title.setTextColor(ContextCompat.getColor(context, R.color.dark_yellow));
        viewHolder.title.setText(getHeaderText(header));
        viewHolder.content.setVisibility(View.GONE);
        viewHolder.content.setPaintFlags(viewHolder.title.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        viewHolder.container.setOnClickListener(null);
        viewHolder.dataContainer.setBackground(null);
        viewHolder.time.setVisibility(View.GONE);
        viewHolder.timeContainer.setVisibility(View.GONE);
        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
        layoutParams.setFullSpan(true);
    }

    private Spanned getHeaderText(Header header) {
        if (DateUtils.isSameDay(new Date(), DateUtil.parseDate(header.getContent()))) {
            if (header instanceof TodoHeader) {
                String color = "#EB4537";
                long doneTodoCount = ((TodoHeader) header).getDoneTodoCount();
                long totalTodoCount = ((TodoHeader) header).getTotalTodoCount();
                if (doneTodoCount / totalTodoCount > 0.7 && totalTodoCount - doneTodoCount < 5) {
                    color = "#55AF7B";
                }
                String html = String.format(
                        "<span>%s</span>&nbsp;" +
                                "<span style='color:" + color + ";'>[%s]</span>" +
                                "<span>%s</span>",
                        header.getContent(),
                        totalTodoCount - doneTodoCount,
                        " &lt;- Now"
                );
                return HtmlUtil.fromHtml(html);
            }
        }
        return new SpannableString(header.getContent());
    }

    @Override
    protected void initHeadEvent(int position, final Header header, TodoViewHolder viewHolder) {
        super.initHeadEvent(position, header, viewHolder);
        viewHolder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleReadStatePreference();
                EventBus.getDefault().post(new RefreshTodoListEvent(null, header.getContent()));
            }

            private void toggleReadStatePreference() {
                if (context.getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                        .getBoolean(IS_SHOW_READ_FLAG_DONE, false)) {
                    context.getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                            .putBoolean(IS_SHOW_READ_FLAG_DONE, false).apply();
                } else {
                    context.getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                            .putBoolean(IS_SHOW_READ_FLAG_DONE, true).apply();
                }
            }
        });
    }

    @Override
    protected void initContentDisplay(TodoViewHolder viewHolder, Todo todo, int position) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(13);
        gradientDrawable.setStroke(1, ContextCompat.getColor(context, R.color.dark_gray));
        if (todo.getPriority() != null&&todo.getPriority() > 0
                &&!todo.getReadFlag().equals(DocumentConst.READ_FLAG_DONE)){
            gradientDrawable.setStroke(2, ContextCompat.getColor(context, R.color.google_red));
        }
        viewHolder.dataContainer.setBackground(gradientDrawable);
        viewHolder.checkbox.setVisibility(View.VISIBLE);
        viewHolder.title.setVisibility(View.GONE);
        if (todo.getReadFlag().equals(DocumentConst.READ_FLAG_DONE)) {
            viewHolder.checkbox.setChecked(true);
            viewHolder.content.setPaintFlags(viewHolder.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            viewHolder.content.setTextColor(ContextCompat.getColor(context, R.color.medium_gray));
            viewHolder.content.setTextSize(8);
        } else {
            viewHolder.checkbox.setChecked(false);
            viewHolder.content.setPaintFlags(viewHolder.title.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            viewHolder.content.setTextColor(ContextCompat.getColor(context, R.color.darker_gray));
            viewHolder.content.setTextSize(12);
        }

        viewHolder.content.setVisibility(View.VISIBLE);
        viewHolder.content.setText(todo.getContentWithoutTime());
        if (todo.getReminder() != null
                && todo.getReminder().getCalendarEventId() != null
                && todo.getReminder().getStart() != null) {

            viewHolder.timeContainer.setVisibility(View.VISIBLE);
            viewHolder.time.setVisibility(View.VISIBLE);
            viewHolder.time.setText(DateFormatUtils.format(todo.getReminder().getStart(), "MM/dd HH:mm"));
            long start = todo.getReminder().getStart().getTime();
            long curr = System.currentTimeMillis();
            long duration = start - curr;
            if (duration > 0) {
                long x = (long) Math.ceil(duration / (1000 * 60 * 30f));
                int colorResId = colorResIds[(int) (Math.log(x) / Math.log(2)) > 2 ? 3 : (int) (Math.log(x) / Math.log(2))];
                viewHolder.time.setTextColor(ContextCompat.getColor(context, colorResId));
                if (viewHolder.checkbox.isChecked()) { // 完成了的时候
                    viewHolder.time.setTextColor(ContextCompat.getColor(context, R.color.darker_gray));
                }
            } else {
                viewHolder.time.setTextColor(ContextCompat.getColor(context, R.color.darker_gray));
            }

        } else {
            viewHolder.timeContainer.setVisibility(View.GONE);
        }
    }

    @Override
    protected void initContentEvent(final TodoViewHolder viewHolder, final Todo todo, final int position) {
        viewHolder.checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    todo.setReadFlag(DocumentConst.READ_FLAG_DONE);
                    EventBus.getDefault().post(new CompleteTodoEvent(todo));
                } else {
                    todo.setReadFlag(DocumentConst.READ_FLAG_NORMAL);
                    EventBus.getDefault().post(new InCompleteTodoEvent(todo));
                }
            }
        });

        viewHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowTodoEditEvent showTodoEditEvent = new ShowTodoEditEvent(v);
                EventBus.getDefault().post(showTodoEditEvent);
                FillTodoModifyEvent event = new FillTodoModifyEvent(todo);
                event.setIndex(itemDataGenerator.getIndex(todo));
                EventBus.getDefault().post(event);
            }
        });

        viewHolder.container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                AnimatedVectorDrawable animatedVectorDrawable;
                if (isDefaultPriority(todo)) {
                    animatedVectorDrawable = (AnimatedVectorDrawable) context.getResources().getDrawable(R.drawable.animated_vector_rect_large, null);
                } else {
                    animatedVectorDrawable = (AnimatedVectorDrawable) context.getResources().getDrawable(R.drawable.animated_vector_rect_reserve_large, null);
                }
                viewHolder.dataContainer.setBackground(animatedVectorDrawable);
                animatedVectorDrawable.start();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    animatedVectorDrawable.registerAnimationCallback(new Animatable2.AnimationCallback() {
                        @Override
                        public void onAnimationEnd(Drawable drawable) {
                            super.onAnimationEnd(drawable);
                            if (isDefaultPriority(todo)) {
                                todo.setPriority(DocumentConst.PRIORITY_FOCUS);
                            } else {
                                todo.setPriority(DocumentConst.PRIORITY_DEFAULT);
                            }
                            EventBus.getDefault().post(new UpdateTodoPriorityEvent(todo));
                        }
                    });
                }

                return true;
            }
        });
    }

}
