package com.beyond.note5.view.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.beyond.note5.MyApplication;
import com.beyond.note5.R;
import com.beyond.note5.bean.Account;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.AddTodoAllSuccessEvent;
import com.beyond.note5.event.HideFABEvent;
import com.beyond.note5.event.InCompleteTodoEvent;
import com.beyond.note5.event.RefreshTodoListEvent;
import com.beyond.note5.event.ScrollToTodoByDateEvent;
import com.beyond.note5.event.ScrollToTodoEvent;
import com.beyond.note5.event.ShowFABEvent;
import com.beyond.note5.event.TodoSyncEvent;
import com.beyond.note5.event.UpdateTodoAllSuccessEvent;
import com.beyond.note5.event.todo.AddTodoSuccessEvent;
import com.beyond.note5.event.todo.CompleteTodoEvent;
import com.beyond.note5.event.todo.DeleteTodoSuccessEvent;
import com.beyond.note5.event.todo.UpdateTodoPriorityEvent;
import com.beyond.note5.event.todo.UpdateTodoSuccessEvent;
import com.beyond.note5.inject.BeanInjectUtils;
import com.beyond.note5.inject.SingletonInject;
import com.beyond.note5.presenter.CalendarPresenterImpl;
import com.beyond.note5.presenter.PredictPresenterImpl;
import com.beyond.note5.presenter.SyncPresenter;
import com.beyond.note5.presenter.TodoCompositePresenter;
import com.beyond.note5.presenter.TodoCompositePresenterImpl;
import com.beyond.note5.presenter.TodoPresenterImpl;
import com.beyond.note5.presenter.TodoSyncPresenterImpl;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.view.DavLoginActivity;
import com.beyond.note5.view.SyncView;
import com.beyond.note5.view.TodoView;
import com.beyond.note5.view.adapter.list.DocumentRecyclerViewAdapter;
import com.beyond.note5.view.adapter.list.RecyclerViewTopMargin;
import com.beyond.note5.view.adapter.list.TodoRecyclerViewAdapter;
import com.beyond.note5.view.adapter.list.header.Header;
import com.beyond.note5.view.adapter.list.header.ItemDataGenerator;
import com.beyond.note5.view.adapter.list.header.ReminderTimeItemDataGenerator;
import com.beyond.note5.view.adapter.view.CalendarViewAdapter;
import com.beyond.note5.view.adapter.view.DocumentViewBase;
import com.beyond.note5.view.adapter.view.PredictViewAdapter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.beyond.note5.model.TodoModelImpl.IS_SHOW_READ_FLAG_DONE;

/**
 * @author: beyond
 * @date: 2019/1/30
 */
@SuppressWarnings("unchecked")
public class TodoListFragment extends Fragment {

    protected RecyclerView recyclerView;

    private SwipeRefreshLayout refreshLayout;

    protected DocumentRecyclerViewAdapter recyclerViewAdapter;
    protected List<Todo> data = new ArrayList<>();

    MyTodoView todoView = new MyTodoView();

    MyCalendarView calendarView  = new MyCalendarView();

    MyPredictView predictView = new MyPredictView();

    MySyncView syncView = new MySyncView();

    TodoCompositePresenter todoCompositePresenter;

    private SyncPresenter syncPresenter;

    @SingletonInject
    private Handler handler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        recyclerViewAdapter = new TodoRecyclerViewAdapter(this.getActivity(), new ReminderTimeItemDataGenerator(data));
        initInjection();
    }

    private void initInjection() {
        BeanInjectUtils.inject(this);
        todoCompositePresenter = new TodoCompositePresenterImpl.Builder(new TodoPresenterImpl(todoView))
                .calendarPresenter(new CalendarPresenterImpl(getActivity(), calendarView))
                .predictPresenter(new PredictPresenterImpl(predictView))
                .build();

        syncPresenter = new TodoSyncPresenterImpl(syncView,handler);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_todo_list, container, false);
        initView(viewGroup);
        initEvent();
        todoCompositePresenter.findAll();
        return viewGroup;
    }

    private void initView(ViewGroup viewGroup) {
        refreshLayout = viewGroup.findViewById(R.id.todo_refresh_layout);
        refreshLayout.setProgressViewOffset(false, 100, 300);
        recyclerView = viewGroup.findViewById(R.id.todo_recycler_view);
        recyclerView.setAdapter(recyclerViewAdapter);
        //设置显示格式
        final StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.addItemDecoration(new RecyclerViewTopMargin());
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void initEvent() {

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!checkAccount()) {
                    return;
                }
                syncPresenter.sync();
            }
        });

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

    private boolean checkAccount() {
        List<Account> all = MyApplication.getInstance().getAccountModel().findAllValid();
        if (all == null || all.isEmpty()) {
            stopRefresh();
            Intent intent = new Intent(getContext(), DavLoginActivity.class);
            startActivity(intent);
            return false;
        }
        return true;
    }

    private void stopRefresh() {
        refreshLayout.setRefreshing(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(AddTodoSuccessEvent event) {
        Todo todo = event.get();
        todoView.onAddSuccess(todo);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(UpdateTodoSuccessEvent event) {
        Todo todo = event.get();
        todoView.onUpdateSuccess(todo);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(AddTodoAllSuccessEvent event) {
        todoCompositePresenter.findAll();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 1)
    public void onReceived(UpdateTodoAllSuccessEvent event) {
        todoCompositePresenter.findAll();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(DeleteTodoSuccessEvent event) {
        Todo todo = event.get();
        todoView.onDeleteSuccess(todo);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(UpdateTodoPriorityEvent event) {
        Todo todo = event.get();
        todoCompositePresenter.updatePriority(todo);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(RefreshTodoListEvent event) {
        todoCompositePresenter.findAll();
        //滚动到当前时间的header， recyclerView 的scrollTo 滚动只会在item 不在显示范围的时候才会触发（Strange）
        //所以要用 staggered.. linear..LayoutManager 的scrollToPositionWithOffset 方法 0 代表top
        String clickContent = event.getClickContent();
        List<Header> headerData = recyclerViewAdapter.getItemDataGenerator().getHeaderData();
        for (final Header headerDatum : headerData) {
            if (StringUtils.equalsIgnoreCase(headerDatum.getContent(),
                    clickContent != null ? clickContent : DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd"))) {
                StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                layoutManager.scrollToPositionWithOffset(headerDatum.getPosition(), 0);
                break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(CompleteTodoEvent event) {
        todoCompositePresenter.completeTodo(event.get());

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(InCompleteTodoEvent event) {
        todoCompositePresenter.inCompleteTodo(event.get());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(ScrollToTodoEvent event) {
        Todo todo = event.get();
        int position = recyclerViewAdapter.getItemDataGenerator().getPosition(todo);
        recyclerView.scrollToPosition(position);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(ScrollToTodoByDateEvent event) {
        Date changedRemindStart = event.get();
        List headerData = recyclerViewAdapter.getItemDataGenerator().getHeaderData();
        for (Object headerDatum : headerData) {
            if (headerDatum instanceof Header) {
                String content = ((Header) headerDatum).getContent();
                if (StringUtils.equals(DateFormatUtils.format(changedRemindStart, "yyyy-MM-dd"), content)) {
                    StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                    layoutManager.scrollToPositionWithOffset(((Header) headerDatum).getPosition(), 0);
                    break;
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(TodoSyncEvent event) {
        syncPresenter.sync();
    }

    private boolean isNeedTrain(Todo oldTodo, Todo newTodo) {
        return !StringUtils.equals(StringUtils.trim(oldTodo.getContent()), StringUtils.trim(newTodo.getContent()));
    }

    public void scrollTo(Integer index) {
        ItemDataGenerator itemDataGenerator = recyclerViewAdapter.getItemDataGenerator();
        Object note = itemDataGenerator.getContentData().get(index);
        int position = itemDataGenerator.getPosition(note);
        recyclerView.scrollToPosition(position);
    }

    public View findViewBy(Integer index) {
        ItemDataGenerator itemDataGenerator = recyclerViewAdapter.getItemDataGenerator();
        if (itemDataGenerator.getContentData().isEmpty()){
            return null;
        }
        Object note = itemDataGenerator.getContentData().get(index);
        int position = itemDataGenerator.getPosition(note);
        return recyclerView.getLayoutManager().findViewByPosition(position);
    }

    private class MyTodoView extends DocumentViewBase<Todo> implements TodoView {

        @Override
        public DocumentRecyclerViewAdapter getRecyclerViewAdapter() {
            return recyclerViewAdapter;
        }

        @Override
        public RecyclerView getRecyclerView() {
            return recyclerView;
        }

        @Override
        public List<Todo> getData() {
            return data;
        }

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
                    //训练
                    if (isNeedTrain(oldT, todo)) {
                        todoCompositePresenter.train(todo.getContent());
                    }
                    break;
                }
            }
        }


        @Override
        public void onDeleteReminderSuccess(Todo todo) {

        }

        @Override
        public void onDeleteReminderFail(Todo todo) {

        }


    }

    private class MyPredictView extends PredictViewAdapter {

    }

    private class MyCalendarView extends CalendarViewAdapter {
    }

    private class MySyncView implements SyncView {
        @Override
        public void onSyncSuccess(String msg) {
            stopRefresh();
            ToastUtil.toast(getContext(), msg);
        }

        @Override
        public void onSyncFail(String msg) {
            stopRefresh();
            ToastUtil.toast(getContext(), msg);
        }
    }
}
