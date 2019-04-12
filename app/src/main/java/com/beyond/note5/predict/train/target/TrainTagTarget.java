package com.beyond.note5.predict.train.target;

import com.beyond.note5.predict.params.TagTarget;

public class TrainTagTarget implements TagTarget<String> {

    private String target;

    public TrainTagTarget(){

    }

    public TrainTagTarget(String target) {
        this.target = target;
    }

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public void setTarget(String target) {
        this.target = target;
    }
}
