package com.beyond.note5.view.adapter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beyond.note5.bean.Todo;
import com.beyond.note5.module.DaggerTodoComponent;
import com.beyond.note5.module.PredictModule;
import com.beyond.note5.module.TodoComponent;
import com.beyond.note5.module.TodoModule;
import com.beyond.note5.predict.bean.Tag;
import com.beyond.note5.presenter.CalendarPresenter;
import com.beyond.note5.presenter.PredictPresenter;
import com.beyond.note5.presenter.TodoPresenter;
import com.beyond.note5.view.CalendarView;
import com.beyond.note5.view.PredictView;
import com.beyond.note5.view.TodoView;
import com.beyond.note5.view.adapter.list.TodoRecyclerViewAdapter;
import com.beyond.note5.view.adapter.list.header.ReminderTimeItemDataGenerator;

import java.util.List;

import javax.inject.Inject;


public abstract class AbstractTodoFragment extends AbstractDocumentFragment<Todo> implements TodoView, CalendarView, PredictView {

    protected ViewGroup viewGroup;

    @Inject
    protected TodoPresenter todoPresenter;
    @Inject
    protected CalendarPresenter calendarPresenter;
    @Inject
    protected PredictPresenter predictPresenter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recyclerViewAdapter = new TodoRecyclerViewAdapter(this.getActivity(), new ReminderTimeItemDataGenerator(data));
        initInjection();
    }

    private void initInjection() {
        TodoComponent todoComponent = DaggerTodoComponent.builder()
                .todoModule(new TodoModule(getActivity(),this,this))
                .predictModule(new PredictModule(this))
                .build();
        todoComponent.inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = initViewGroup(inflater,container,savedInstanceState);
        initView();
        initEvent();
        todoPresenter.findAll();
        return viewGroup;
    }

    protected abstract void initView();

    protected abstract void initEvent();

    protected abstract ViewGroup initViewGroup(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    @Override
    public void onEventAddSuccess(Todo todo) {
        msg("成功添加到日历事件");
    }

    @Override
    public void onEventAddFail(Todo todo) {
        msg("添加到日历事件失败");
    }

    @Override
    public void onEventFindAllSuccess(List<Todo> allTodo) {
        msg("成功查询日历事件");
    }

    @Override
    public void onEventFindAllFail() {
        msg("查询日历事件失败");
    }

    @Override
    public void onEventDeleteSuccess(Todo todo) {
        msg("成功删除日历事件");
    }

    @Override
    public void onEventDeleteFail(Todo todo) {
        msg("删除日历事件失败");
    }

    @Override
    public void onEventUpdateSuccess(Todo todo) {
        msg("成功更新日历事件");
    }

    @Override
    public void onEventUpdateFail(Todo todo) {
        msg("更新日历事件失败");
    }

    @Override
    public void onCalendarReminderDeleteSuccess(Todo todo) {
        msg("取消提醒成功");
    }

    @Override
    public void onCalendarReminderDeleteFail(Todo todo) {
        msg("取消提醒失败");
    }

    @Override
    public void onCalendarReminderRestoreSuccess(Todo todo) {
        msg("恢复提醒成功");
    }

    @Override
    public void onCalendarReminderRestoreFail(Todo todo) {
        msg("恢复提醒失败");
    }

    @Override
    public void onDeleteReminderSuccess(Todo todo) {
        msg("成功删除提醒");
    }

    @Override
    public void onDeleteReminderFail(Todo todo) {
        msg("删除提醒失败");
    }

    @Override
    public void onPredictSuccess(List<Tag> data, String source) {
        msg("预测成功");
    }

    @Override
    public void onPredictFail() {
        msg("预测失败");
    }

    @Override
    public void onTrainSuccess() {
        // do nothing
        // msg("训练成功");
    }

    @Override
    public void onTrainFail() {
        msg("网络状况不佳");
    }

}
