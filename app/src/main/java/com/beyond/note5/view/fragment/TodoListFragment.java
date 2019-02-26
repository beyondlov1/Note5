package com.beyond.note5.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beyond.note5.R;
import com.beyond.note5.event.AddTodoEvent;
import com.beyond.note5.event.CompleteTodoEvent;
import com.beyond.note5.event.DeleteTodoEvent;
import com.beyond.note5.event.RefreshTodoListEvent;
import com.beyond.note5.event.UpdateTodoEvent;
import com.beyond.note5.module.DaggerTodoComponent;
import com.beyond.note5.module.TodoComponent;
import com.beyond.note5.module.TodoModule;
import com.beyond.note5.presenter.TodoPresenter;
import com.beyond.note5.view.adapter.AbstractFragmentTodoView;
import com.beyond.note5.view.adapter.component.TodoRecyclerViewAdapter;
import com.beyond.note5.view.adapter.component.header.LastModifyTimeItemDataGenerator;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

/**
 * @author: beyond
 * @date: 2019/1/30
 */

public class TodoListFragment extends AbstractFragmentTodoView {

    @Inject
    TodoPresenter todoPresenter;

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
        initView(viewGroup);
        todoPresenter.findAll();
        return viewGroup;
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
        todoPresenter.delete(event.get());
    }
}
