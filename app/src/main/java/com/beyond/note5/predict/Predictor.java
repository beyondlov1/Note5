package com.beyond.note5.predict;

import com.beyond.note5.predict.bean.Tag;
import com.beyond.note5.predict.params.PredictCallback;
import com.beyond.note5.predict.filter.train.TrainFilter;
import com.beyond.note5.predict.train.TrainSource;

import java.util.List;
import java.util.concurrent.ExecutorService;

public interface Predictor<S, T> {
    void setExecutorService(ExecutorService executorService);

    List<Tag> predictSync(S s);
    void predictAsync(S s, PredictCallback<S,T> predictCallback);

    void addTrainFilter(TrainFilter filter);
    void trainSync(TrainSource trainTagTarget) throws Exception;
    void trainAsync(TrainSource trainTagTarget) throws Exception;
}
