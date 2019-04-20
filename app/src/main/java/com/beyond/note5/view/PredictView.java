package com.beyond.note5.view;

import com.beyond.note5.predict.bean.Tag;

import java.util.List;

public interface PredictView {
    void onPredictSuccess(List<Tag> data, String source);

    void onPredictFail();

    void onTrainSuccess();

    void onTrainFail();
}
