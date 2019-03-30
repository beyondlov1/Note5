package com.beyond.note5.predict;

import com.beyond.note5.predict.bean.Tag;

import java.util.List;
import java.util.concurrent.ExecutorService;

public interface TagPredictor<S, T> {
    void setExecutorService(ExecutorService executorService);
    void predict(S s, Callback<S,T> callback);
    List<Tag> predict(S s);
    TagTrainer getTagTrainer();
}
