package com.beyond.note5.predict.train;

import com.beyond.note5.predict.bean.TagGraph;

public interface Trainer {
    void trainSync(TrainSource source) throws Exception ;
    void trainAsync(TrainSource source) throws Exception;
    TagGraph getTagGraph();
}
