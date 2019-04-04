package com.beyond.note5.predict;

import com.beyond.note5.predict.bean.TagGraph;
import com.beyond.note5.predict.filter.target.TrainTarget;

public interface TagTrainer {
    void trainSync(TrainTarget target) throws Exception ;
    TagGraph getTagGraph();
}
