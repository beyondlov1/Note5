package com.beyond.note5.predict;

import com.beyond.note5.predict.bean.Callback;

public interface TagPredictor<S, T> {
    void predict(S s, Callback<S,T> callback);
}
