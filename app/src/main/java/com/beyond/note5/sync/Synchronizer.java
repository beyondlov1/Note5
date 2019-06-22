package com.beyond.note5.sync;

public interface Synchronizer<T> {
    boolean sync() throws Exception;
}
