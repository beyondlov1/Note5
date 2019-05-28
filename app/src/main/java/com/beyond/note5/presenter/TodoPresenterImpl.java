package com.beyond.note5.presenter;

import android.support.annotation.Nullable;

import com.beyond.note5.bean.Todo;
import com.beyond.note5.model.TodoModel;
import com.beyond.note5.model.TodoModelImpl;
import com.beyond.note5.view.TodoView;

import java.util.List;

public class TodoPresenterImpl implements TodoPresenter {
    private TodoView todoView;
    private TodoModel todoModel;

    public TodoPresenterImpl(@Nullable TodoView todoView) {
        this.todoView = todoView;
        this.todoModel = new TodoModelImpl();
    }

    @Override
    public void add(Todo note) {
        try {
            todoModel.add(note);
            this.addSuccess(note);
        } catch (Exception e) {
            e.printStackTrace();
            this.addFail(note);
        }
    }

    @Override
    public void addSuccess(Todo note) {
        if (todoView == null) {
            return;
        }
        todoView.onAddSuccess(note);
    }

    @Override
    public void addFail(Todo note) {
        if (todoView == null) {
            return;
        }
        todoView.onAddFail(note);
    }

    @Override
    public void update(Todo note) {
        try {
            todoModel.update(note);
            this.updateSuccess(note);
        } catch (Exception e) {
            e.printStackTrace();
            this.updateFail(note);
        }
    }

    @Override
    public void updateSuccess(Todo note) {
        if (todoView == null) {
            return;
        }
        todoView.onUpdateSuccess(note);
    }

    @Override
    public void updateFail(Todo note) {
        if (todoView == null) {
            return;
        }
        todoView.onUpdateFail(note);
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
    public void delete(Todo note) {
        try {
            todoModel.delete(note);
            this.deleteSuccess(note);
        } catch (Exception e) {
            e.printStackTrace();
            this.deleteFail(note);
        }
    }

    @Override
    public void deleteSuccess(Todo note) {
        if (todoView == null) {
            return;
        }
        todoView.onDeleteSuccess(note);
    }

    @Override
    public void deleteFail(Todo note) {
        if (todoView == null) {
            return;
        }
        todoView.onDeleteFail(note);
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
}
