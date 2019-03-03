package com.beyond.note5.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beyond.note5.MyApplication;
import com.beyond.note5.R;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.AddTodoEvent;
import com.beyond.note5.event.CompleteTodoEvent;
import com.beyond.note5.event.DeleteTodoEvent;
import com.beyond.note5.event.HideFABEvent;
import com.beyond.note5.event.RefreshTodoListEvent;
import com.beyond.note5.event.ShowFABEvent;
import com.beyond.note5.event.UpdateTodoEvent;
import com.beyond.note5.module.DaggerTodoComponent;
import com.beyond.note5.module.TodoComponent;
import com.beyond.note5.module.TodoModule;
import com.beyond.note5.presenter.TodoPresenter;
import com.beyond.note5.view.adapter.AbstractFragmentTodoView;
import com.beyond.note5.view.adapter.component.TodoRecyclerViewAdapter;
import com.beyond.note5.view.adapter.component.header.LastModifyTimeItemDataGenerator;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import static com.beyond.note5.model.TodoModelImpl.IS_SHOW_READ_FLAG_DONE;

/**
 * @author: beyond
 * @date: 2019/1/30
 */

public class TodoListFragment extends AbstractFragmentTodoView {

    @Inject
    TodoPresenter todoPresenter;

    private View container;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recyclerViewAdapter = new TodoRecyclerViewAdapter(this.getActivity(), new LastModifyTimeItemDataGenerator<>(data));
        initInjection();
    }

    private void initInjection() {
        TodoComponent todoComponent = DaggerTodoComponent.builder().todoModule(new TodoModule(this)).build();
        todoComponent.inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup=(ViewGroup) inflater.inflate(R.layout.fragment_todo_list,container,false);
        this.container = viewGroup;
        initView(viewGroup);
        todoPresenter.findAll();
        initOnScrollListener();
        return viewGroup;
    }

    private void initOnScrollListener() {
        recyclerView.setOnFlingListener(new RecyclerView.OnFlingListener() {
            @Override
            public boolean onFling(int velocityX, int velocityY) {
                //上划
                if (velocityY<0){
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
                if(!recyclerView.canScrollVertically(1)&&!recyclerView.canScrollVertically(-1)){
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(AddTodoEvent event) {
        todoPresenter.add(event.get());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(UpdateTodoEvent event) {
        todoPresenter.update(event.get());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(DeleteTodoEvent event) {
        todoPresenter.delete(event.get());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(RefreshTodoListEvent event) {
        todoPresenter.findAll();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(CompleteTodoEvent event) {
        todoPresenter.update(event.get());
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
//                recyclerView.scrollToPosition(insertIndex);
                msg("更新成功");
                break;
            }
        }
    }

    @Override
    public void onFindAllSuccess(List<Todo> allT) {
        super.onFindAllSuccess(allT);

        //TODO: todo一个都没有时特殊处理
        if (allT.isEmpty()){
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                            .putBoolean(IS_SHOW_READ_FLAG_DONE,true).apply();
                    EventBus.getDefault().post(new RefreshTodoListEvent(null));
                }
            });
        }else {
            container.setOnClickListener(null);
        }
    }
}
