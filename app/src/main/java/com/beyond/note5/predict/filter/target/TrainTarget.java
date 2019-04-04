package com.beyond.note5.predict.filter.target;

import com.beyond.note5.predict.filter.Target;

public class TrainTarget implements Target<String> {

    private String target;

    public TrainTarget(){

    }

    public TrainTarget(String target) {
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
