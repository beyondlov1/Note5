package com.beyond.note5.presenter;

import java.util.List;

/**
 * @author: beyond
 * @date: 2019/2/6
 */

public interface DocumentPresenter<T>  extends CRUDPresenter<T>{

    void deleteLogic(T document);

    void updatePriority(T document);

    void updatePrioritySuccess(T document);

    void updatePriorityFail(T document);

    List<T> selectAllInAll();

    T selectById(String id);
}
