package com.beyond.note5.presenter;

/**
 * @author: beyond
 * @date: 2019/2/6
 */

public interface DocumentPresenter<T>  extends CRUDPresenter<T>{

    void updatePriority(T document);

    void updatePrioritySuccess(T document);

    void updatePriorityFail(T document);

}
