package com.beyond.note5.predict;

import java.util.concurrent.ExecutorService;

public interface TagPredictor<S, T> {
    void setExecutorService(ExecutorService executorService);
    void predict(S s, Callback<S,T> callback);
    TagTrainer getTagTrainer();
}
