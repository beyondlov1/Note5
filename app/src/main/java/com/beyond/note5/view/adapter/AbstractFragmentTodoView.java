package com.beyond.note5.view.adapter;

import com.beyond.note5.bean.Todo;
import com.beyond.note5.view.CalendarView;
import com.beyond.note5.view.TodoView;
import com.beyond.note5.view.fragment.AbstractDocumentFragment;

import java.util.List;

public class AbstractFragmentTodoView extends AbstractDocumentFragment<Todo> implements TodoView,CalendarView {

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
    public void onReminderDeleteSuccess(Todo todo) {
        msg("取消提醒成功");
    }

    @Override
    public void onReminderDeleteFail(Todo todo) {
        msg("取消提醒失败");
    }

    @Override
    public void onReminderRestoreSuccess(Todo todo) {
        msg("恢复提醒成功");
    }

    @Override
    public void onReminderRestoreFail(Todo todo) {
        msg("恢复提醒失败");
    }
}
