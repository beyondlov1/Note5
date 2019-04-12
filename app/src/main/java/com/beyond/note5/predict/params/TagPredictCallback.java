package com.beyond.note5.predict.params;

/**
 * @author beyondlov1
 * @date 2019/03/11
 */
public interface TagPredictCallback<S,T> {
    void onSuccess(S s,T t);
    void onFail();
}
