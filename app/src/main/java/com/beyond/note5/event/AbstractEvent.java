package com.beyond.note5.event;

/**
 * Created by beyond on 2019/2/2.
 */

public abstract class AbstractEvent<T> implements Event{
    private T t;

    AbstractEvent(T t){
        this.t = t;
    }

    public T get(){
        return t;
    }
}
