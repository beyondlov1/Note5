package com.beyond.note5.predict;

/**
 * @author beyondlov1
 * @date 2019/03/11
 */
public interface Callback<S,T> {
    void onSuccess(S s,T t);
    void onFail();
}
