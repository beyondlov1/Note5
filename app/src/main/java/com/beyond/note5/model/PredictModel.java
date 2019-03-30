package com.beyond.note5.model;

import com.beyond.note5.predict.bean.Tag;

import java.util.List;

/**
 * @author beyondlov1
 * @date 2019/03/30
 */
public interface PredictModel {
    List<Tag> predict(String source );
    void train(String source) throws Exception;
}
