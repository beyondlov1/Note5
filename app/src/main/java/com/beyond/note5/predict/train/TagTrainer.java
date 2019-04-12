package com.beyond.note5.predict.train;

import com.beyond.note5.predict.bean.TagGraph;
import com.beyond.note5.predict.train.target.TrainTagTarget;

public interface TagTrainer {
    void trainSync(TrainTagTarget target) throws Exception ;
    void trainAsync(TrainTagTarget target) throws Exception;
    TagGraph getTagGraph();
}
