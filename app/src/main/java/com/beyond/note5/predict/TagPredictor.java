package com.beyond.note5.predict;

import com.beyond.note5.predict.bean.Tag;
import com.beyond.note5.predict.params.TagPredictCallback;
import com.beyond.note5.predict.train.filter.AbstractTrainTagFilter;
import com.beyond.note5.predict.train.target.TrainTagTarget;

import java.util.List;
import java.util.concurrent.ExecutorService;

public interface TagPredictor<S, T> {
    void setExecutorService(ExecutorService executorService);

    List<Tag> predictSync(S s);
    void predictAsync(S s, TagPredictCallback<S,T> tagPredictCallback);

    void addTrainFilter(AbstractTrainTagFilter filter);
    void trainSync(TrainTagTarget trainTagTarget) throws Exception;
    void trainAsync(TrainTagTarget trainTagTarget) throws Exception;
}
