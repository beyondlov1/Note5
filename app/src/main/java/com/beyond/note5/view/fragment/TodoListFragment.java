package com.beyond.note5.view.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.*;
import com.beyond.note5.MyApplication;
import com.beyond.note5.R;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.*;
import com.beyond.note5.module.DaggerTodoComponent;
import com.beyond.note5.module.TodoComponent;
import com.beyond.note5.module.TodoModule;
import com.beyond.note5.presenter.CalendarPresenter;
import com.beyond.note5.presenter.TodoPresenter;
import com.beyond.note5.utils.ViewUtil;
import com.beyond.note5.view.adapter.AbstractFragmentTodoView;
import com.beyond.note5.view.adapter.component.TodoRecyclerViewAdapter;
import com.beyond.note5.view.adapter.component.header.Header;
import com.beyond.note5.view.adapter.component.header.ReminderTimeItemDataGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;

import static com.beyond.note5.model.TodoModelImpl.IS_SHOW_READ_FLAG_DONE;

/**
 * @author: beyond
 * @date: 2019/1/30
 */

public class TodoListFragment extends AbstractFragmentTodoView {

    @Inject
    TodoPresenter todoPresenter;
    @Inject
    CalendarPresenter calendarPresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recyclerViewAdapter = new TodoRecyclerViewAdapter(this.getActivity(), new ReminderTimeItemDataGenerator(data));
        initInjection();
    }

    private void initInjection() {
        TodoComponent todoComponent = DaggerTodoComponent.builder().todoModule(new TodoModule(getActivity(),this,this)).build();
        todoComponent.inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_todo_list, container, false);
        initView(viewGroup);
        todoPresenter.findAll();
        initEvent();
        initOnScrollListener();
        return viewGroup;
    }

    private void initOnScrollListener() {
        recyclerView.setOnFlingListener(new RecyclerView.OnFlingListener() {
            @Override
            public boolean onFling(int velocityX, int velocityY) {
                //上划
                if (velocityY < 0) {
                    EventBus.getDefault().post(new ShowFABEvent(R.id.todo_recycler_view));
                }
                return false;
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //未到滚动的高度
                if (!recyclerView.canScrollVertically(1) && !recyclerView.canScrollVertically(-1)) {
                    EventBus.getDefault().post(new ShowFABEvent(R.id.todo_recycler_view));
                    return;
                }
                //下划到底
                if (!recyclerView.canScrollVertically(1)) {
                    EventBus.getDefault().post(new HideFABEvent(R.id.todo_recycler_view));
                }
                //上划到顶
                if (!recyclerView.canScrollVertically(-1)) {
                    EventBus.getDefault().post(new ShowFABEvent(R.id.todo_recycler_view));
                }

            }

        });
    }

    private void initView(ViewGroup viewGroup) {
        recyclerView = viewGroup.findViewById(R.id.todo_recycler_view);
        recyclerView.setAdapter(recyclerViewAdapter);
        //设置显示格式
        final StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initEvent(){

        // 点击切换显示模式（是否显示已读）
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    return false;
                }

                @Override
                public void onShowPress(MotionEvent e) {

                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    if (!getActivity().getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                            .getBoolean(IS_SHOW_READ_FLAG_DONE, false)) {
                        getActivity().getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                                .putBoolean(IS_SHOW_READ_FLAG_DONE, true).apply();
                        EventBus.getDefault().post(new RefreshTodoListEvent(null));
                    } else {
                        getActivity().getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                                .putBoolean(IS_SHOW_READ_FLAG_DONE, false).apply();
                        EventBus.getDefault().post(new RefreshTodoListEvent(null));
                    }
                    return true;
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    return false;
                }

                @Override
                public void onLongPress(MotionEvent e) {

                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    return false;
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(AddTodoEvent event) {
        todoPresenter.add(event.get());
        if (event.get().getReminder()!=null) {
            calendarPresenter.add(event.get());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(UpdateTodoEvent event) {
        todoPresenter.update(event.get());
        if (event.get().getReminder()!=null) {
            if (event.get().getReminder().getCalendarEventId() == null){
                calendarPresenter.add(event.get());
            }else {
                calendarPresenter.update(event.get());
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(DeleteTodoEvent event) {
        todoPresenter.delete(event.get());
        if (event.get().getReminder()!=null) {
            calendarPresenter.delete(event.get());
        }
    }

    @SuppressWarnings("unchecked")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(RefreshTodoListEvent event) {
        todoPresenter.findAll();
        //滚动到当前时间的header， recyclerView 的scrollTo 滚动只会在item 不在显示范围的时候才会触发（Strange）
        //所以要用 staggered.. linear..LayoutManager 的scrollToPositionWithOffset 方法 0 代表top
        String clickContent = event.getClickContent();
        List<Header> headerData = recyclerViewAdapter.getItemDataGenerator().getHeaderData();
        for (final Header headerDatum : headerData) {
            if (StringUtils.equalsIgnoreCase(headerDatum.getContent(),
                    clickContent!=null?clickContent:DateFormatUtils.format(System.currentTimeMillis(),"yyyy-MM-dd"))){
                StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager)recyclerView.getLayoutManager();
                layoutManager.scrollToPositionWithOffset(headerDatum.getPosition(),0);
                break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(CompleteTodoEvent event) {
        todoPresenter.update(event.get());
        calendarPresenter.deleteReminder(event.get());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(InCompleteTodoEvent event) {
        todoPresenter.update(event.get());
        calendarPresenter.restoreReminder(event.get());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onUpdateSuccess(Todo todo) {
        Iterator<Todo> iterator = data.iterator();
        while (iterator.hasNext()) {
            Todo oldT = iterator.next();
            if (StringUtils.equals(oldT.getId(), todo.getId())) {
                iterator.remove();
                recyclerViewAdapter.notifyRemoved(oldT);
                int insertIndex = recyclerViewAdapter.getItemDataGenerator().getInsertIndex(todo);
                data.add(insertIndex, todo);
                recyclerViewAdapter.notifyInserted(todo);
                msg("更新成功");
                break;
            }
        }
    }
}
