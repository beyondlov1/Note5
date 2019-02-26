package com.beyond.note5.presenter;

import com.beyond.note5.bean.Todo;
import com.beyond.note5.model.TodoModel;
import com.beyond.note5.model.TodoModelImpl;
import com.beyond.note5.view.TodoView;

import java.util.List;

public class TodoPresenterImpl implements TodoPresenter {
    private TodoView todoView;
    private TodoModel todoModel;

    public TodoPresenterImpl(TodoView todoView) {
        this.todoView = todoView;
        this.todoModel = new TodoModelImpl();
    }

    @Override
    public void add(Todo note) {
        try {
            todoModel.add(note);
            this.addSuccess(note);
        }catch (Exception e){
            e.printStackTrace();
            this.addFail(note);
        }
    }

    @Override
    public void addSuccess(Todo note) {
        todoView.onAddSuccess(note);
    }

    @Override
    public void addFail(Todo note) {
        todoView.onAddFail(note);
    }

    @Override
    public void update(Todo note) {
        try {
            todoModel.update(note);
            this.updateSuccess(note);
        }catch (Exception e){
            e.printStackTrace();
            this.updateFail(note);
        }
    }

    @Override
    public void updateSuccess(Todo note) {
        todoView.onUpdateSuccess(note);
    }

    @Override
    public void updateFail(Todo note) {
        todoView.onUpdateFail(note);
    }

    @Override
    public void delete(Todo note) {
        try {
            todoModel.delete(note);
            this.deleteSuccess(note);
        }catch (Exception e){
            e.printStackTrace();
            this.deleteFail(note);
        }
    }

    @Override
    public void deleteSuccess(Todo note) {
        todoView.onDeleteSuccess(note);
    }

    @Override
    public void deleteFail(Todo note) {
        todoView.onDeleteFail(note);
    }

    @Override
    public void findAll() {
        try {
            List<Todo> allTodo = todoModel.findAll();
            this.findAllSuccess(allTodo);
        }catch (Exception e){
            e.printStackTrace();
            this.findAllFail();
        }
    }

    @Override
    public void findAllSuccess(List<Todo> allTodo) {
        todoView.onFindAllSuccess(allTodo);
    }

    @Override
    public void findAllFail() {
        todoView.onFindAllFail();
    }
}
