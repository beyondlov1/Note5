package com.beyond.note5.utils;

/**
 * @author: beyond
 * @date: 2019/7/15
 */

public interface Callback<T,V> {
    void onSuccess(T t);
    void onFail(V v);
}
