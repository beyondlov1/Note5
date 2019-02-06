package com.beyond.note5.presenter;

import java.util.List;

/**
 * @author: beyond
 * @date: 2019/2/6
 */

public interface DocumentPresenter<T> {
    void add(T document);

    void addSuccess(T document);

    void addFail(T document);

    void update(T document);

    void updateSuccess(T document);

    void updateFail(T document);

    void delete(T document);

    void deleteSuccess(T document);

    void deleteFail(T document);

    void findAll();

    void findAllSuccess(List<T> allDocument);

    void findAllFail();
}
