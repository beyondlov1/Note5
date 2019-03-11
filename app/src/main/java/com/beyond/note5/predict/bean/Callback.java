package com.beyond.note5.predict.bean;

/**
 * @author beyondlov1
 * @date 2019/03/11
 */
public interface Callback<T> {
    void onSuccess(T t);
    void onFail();
}
