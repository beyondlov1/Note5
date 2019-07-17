package com.beyond.note5.view;

import java.util.List;

/**
 * @author: beyond
 * @date: 2019/2/6
 */

public interface DocumentView<T> {
    void onAddSuccess(T document);

    void onAddFail(T document);

    void onFindAllSuccess(List<T> allDocument);

    void onFindAllFail();

    void onDeleteFail(T document);

    void onDeleteSuccess(T document);

    void onUpdateFail(T document);

    void onUpdateSuccess(T document);

    void onUpdatePrioritySuccess(T document);

    void onUpdatePriorityFail(T document);

    void onAddAllSuccess(List<T> addList);

    void onAddAllFail(Exception e);

    void onUpdateAllSuccess(List<T> updateList);

    void onUpdateAllFail(Exception e);
}
