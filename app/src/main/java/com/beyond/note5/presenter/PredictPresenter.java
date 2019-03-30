package com.beyond.note5.presenter;

import com.beyond.note5.predict.bean.Tag;

import java.util.List;

/**
 * @author beyondlov1
 * @date 2019/03/30
 */
public interface PredictPresenter {
    void predict(String source);
    void onPredictSuccess(List<Tag> data);
    void onPredictFail();

    void train(String source);
    void onTrainSuccess();
    void onTrainFail();
}
