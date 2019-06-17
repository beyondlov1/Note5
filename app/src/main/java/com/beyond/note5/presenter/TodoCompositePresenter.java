package com.beyond.note5.presenter;

import com.beyond.note5.bean.Todo;

public interface TodoCompositePresenter {
    void add(Todo todo);

    void update(Todo todo);

    void delete(Todo todo);

    void deleteLogic(Todo todo);

    void findAll();

    void predict(String substring);

    void train(String content);

    void updatePriority(Todo todo);

    void deleteReminder(Todo todo);

    void completeTodo(Todo todo);

    void inCompleteTodo(Todo todo);
}
