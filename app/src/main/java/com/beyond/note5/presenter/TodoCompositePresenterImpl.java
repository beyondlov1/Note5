package com.beyond.note5.presenter;

import android.support.annotation.NonNull;

import com.beyond.note5.bean.Todo;

public class TodoCompositePresenterImpl implements TodoCompositePresenter {

    private TodoPresenter todoPresenter;
    private CalendarPresenter calendarPresenter;
    private PredictPresenter predictPresenter;

    private TodoCompositePresenterImpl(){
    }

    @Override
    public void add(Todo todo) {
        todoPresenter.add(todo);
        if (todo.getReminder() != null) {
            if (calendarPresenter == null){
                return;
            }
            calendarPresenter.add(todo);
        }
        if (predictPresenter==null){
            return;
        }
        predictPresenter.train(todo.getContent());
    }

    @Override
    public void update(Todo todo) {
        todoPresenter.update(todo);
        if (todo.getReminder()!=null) {
            if (calendarPresenter == null){
                return;
            }
            if (todo.getReminder().getCalendarEventId() == null){
                calendarPresenter.add(todo);
            }else {
                calendarPresenter.update(todo);
            }
        }
    }

    @Override
    public void delete(Todo todo) {
        todoPresenter.delete(todo);
        if (todo.getReminder()!=null) {
            if (calendarPresenter == null){
                return;
            }
            calendarPresenter.delete(todo);
        }
    }

    @Override
    public void deleteLogic(Todo todo) {
        todoPresenter.deleteLogic(todo);
        if (todo.getReminder()!=null) {
            if (calendarPresenter == null){
                return;
            }
            calendarPresenter.delete(todo);
        }
    }

    @Override
    public void findAll() {
        todoPresenter.findAll();
    }

    @Override
    public void predict(String substring) {
        if (predictPresenter==null){
            return;
        }
        predictPresenter.predict(substring);
    }

    @Override
    public void train(String content) {
        if (predictPresenter==null){
            return;
        }
        predictPresenter.train(content);
    }

    @Override
    public void updatePriority(Todo todo) {
        todoPresenter.updatePriority(todo);
    }

    @Override
    public void deleteReminder(Todo todo) {
        calendarPresenter.delete(todo);
        todoPresenter.deleteReminder(todo);
        todo.setReminder(null);
    }

    @Override
    public void completeTodo(Todo todo) {
        todoPresenter.update(todo);
        calendarPresenter.deleteReminder(todo);
    }

    @Override
    public void inCompleteTodo(Todo todo) {
        todoPresenter.update(todo);
        calendarPresenter.restoreReminder(todo);
    }

    public static class Builder{
        TodoPresenter todoPresenter;
        CalendarPresenter calendarPresenter;
        PredictPresenter predictPresenter;

        public Builder(@NonNull TodoPresenter todoPresenter){
            this.todoPresenter = todoPresenter;
        }

        public Builder calendarPresenter(CalendarPresenter calendarPresenter){
            this.calendarPresenter = calendarPresenter;
            return this;
        }

        public Builder predictPresenter(PredictPresenter predictPresenter){
            this.predictPresenter = predictPresenter;
            return this;
        }

        public TodoCompositePresenter build(){
            TodoCompositePresenterImpl todoCompositePresenter = new TodoCompositePresenterImpl();
            todoCompositePresenter.todoPresenter = this.todoPresenter;
            todoCompositePresenter.calendarPresenter = this.calendarPresenter;
            todoCompositePresenter.predictPresenter = this.predictPresenter;
            return todoCompositePresenter;
        }
    }
}
