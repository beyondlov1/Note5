package com.beyond.note5.predict.train;

import com.beyond.note5.predict.params.TagSource;

public class TrainSource implements TagSource<String> {

    private String target;

    public TrainSource(){

    }

    public TrainSource(String target) {
        this.target = target;
    }

    @Override
    public String getContent() {
        return target;
    }

    @Override
    public void setContent(String target) {
        this.target = target;
    }
}
