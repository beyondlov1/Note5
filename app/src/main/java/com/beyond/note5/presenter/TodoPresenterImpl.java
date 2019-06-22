package com.beyond.note5.presenter;

import android.support.annotation.Nullable;

import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.todo.AddTodoSuccessEvent;
import com.beyond.note5.event.todo.DeleteTodoSuccessEvent;
import com.beyond.note5.event.todo.UpdateTodoSuccessEvent;
import com.beyond.note5.model.TodoModel;
import com.beyond.note5.model.TodoModelImpl;
import com.beyond.note5.utils.TimeNLPUtil;
import com.beyond.note5.view.TodoView;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class TodoPresenterImpl implements TodoPresenter {
    private TodoView todoView;
    private TodoModel todoModel;

    public TodoPresenterImpl(@Nullable TodoView todoView) {
        this.todoView = todoView;
        this.todoModel = TodoModelImpl.getSingletonInstance();
    }

    @Override
    public void add(Todo todo) {
        try {
            fillContentWithoutTime(todo);
            todoModel.add(todo);
            this.addSuccess(todo);
        } catch (Exception e) {
            e.printStackTrace();
            this.addFail(todo);
        }
    }

    @Override
    public void addSuccess(Todo todo) {
        if (todoView == null) {
            return;
        }
        EventBus.getDefault().post(new AddTodoSuccessEvent(todo));
        todoView.onAddSuccess(todo);
    }

    @Override
    public void addFail(Todo todo) {
        if (todoView == null) {
            return;
        }
        todoView.onAddFail(todo);
    }

    @Override
    public void update(Todo todo) {
        try {
            fillContentWithoutTime(todo);
            todoModel.update(todo);
            this.updateSuccess(todo);
        } catch (Exception e) {
            e.printStackTrace();
            this.updateFail(todo);
        }
    }

    @Override
    public void updateSuccess(Todo todo) {
        if (todoView == null) {
            return;
        }
        EventBus.getDefault().post(new UpdateTodoSuccessEvent(todo));
        todoView.onUpdateSuccess(todo);
    }

    @Override
    public void updateFail(Todo todo) {
        if (todoView == null) {
            return;
        }
        todoView.onUpdateFail(todo);
    }

    @Override
    public void deleteLogic(Todo todo) {
        try {
            todoModel.deleteLogic(todo);
            this.deleteSuccess(todo);
        } catch (Exception e) {
            e.printStackTrace();
            this.deleteFail(todo);
        }
    }

    @Override
    public void updatePriority(Todo document) {
        try {
            todoModel.update(document);
            this.updatePrioritySuccess(document);
        } catch (Exception e) {
            e.printStackTrace();
            this.updatePriorityFail(document);
        }
    }

    @Override
    public void updatePrioritySuccess(Todo document) {
        if (todoView == null) {
            return;
        }
        todoView.onUpdatePrioritySuccess(document);
    }

    @Override
    public void updatePriorityFail(Todo document) {
        if (todoView == null) {
            return;
        }
        todoView.onUpdatePriorityFail(document);
    }

    @Override
    public void delete(Todo todo) {
        try {
            todoModel.delete(todo);
            this.deleteSuccess(todo);
        } catch (Exception e) {
            e.printStackTrace();
            this.deleteFail(todo);
        }
    }

    @Override
    public void deleteSuccess(Todo todo) {
        if (todoView == null) {
            return;
        }
        EventBus.getDefault().post(new DeleteTodoSuccessEvent(todo));
        todoView.onDeleteSuccess(todo);
    }

    @Override
    public void deleteFail(Todo todo) {
        if (todoView == null) {
            return;
        }
        todoView.onDeleteFail(todo);
    }

    @Override
    public List<Todo> selectAllInAll() {
        return todoModel.findAllInAll();
    }

    @Override
    public Todo selectById(String id) {
        return todoModel.findById(id);
    }

    @Override
    public void findAll() {
        try {
            List<Todo> allTodo = todoModel.findAll();
            this.findAllSuccess(allTodo);
        } catch (Exception e) {
            e.printStackTrace();
            this.findAllFail();
        }
    }

    @Override
    public void findAllSuccess(List<Todo> allTodo) {
        if (todoView == null) {
            return;
        }
        todoView.onFindAllSuccess(allTodo);
    }

    @Override
    public void findAllFail() {
        if (todoView == null) {
            return;
        }
        todoView.onFindAllFail();
    }

    @Override
    public void deleteReminder(Todo todo) {
        try {
            todoModel.deleteReminder(todo);
            this.onDeleteReminderSuccess(todo);
        } catch (Exception e) {
            e.printStackTrace();
            this.onDeleteReminderFail(todo);
        }
    }

    @Override
    public void onDeleteReminderSuccess(Todo todo) {
        if (todoView == null) {
            return;
        }
        todoView.onDeleteReminderSuccess(todo);
    }

    @Override
    public void onDeleteReminderFail(Todo todo) {
        if (todoView == null) {
            return;
        }
        todoView.onDeleteReminderFail(todo);
    }

    /**
     * 计算无时间内容
     * 要不要改成异步， 看情况吧
     * @param todo 待办
     */
    private void fillContentWithoutTime(Todo todo) {
        String contentWithoutTime = StringUtils.trim(TimeNLPUtil.getOriginExpressionWithoutTime(StringUtils.trim(todo.getContent())));
        if (StringUtils.isBlank(contentWithoutTime)) {
            contentWithoutTime = StringUtils.trim(todo.getContent());
        }
        todo.setContentWithoutTime(contentWithoutTime);
    }
}
