package com.beyond.note5.predict;

public interface TagPredictor<S, T> {
    void predict(S s, Callback<S,T> callback);
    TagTrainer getTagTrainer();
}
