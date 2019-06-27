package com.beyond.note5.sync.model;

import java.io.IOException;

public interface SharedSource<T> {
    T get() throws IOException;
    void set(T t) throws IOException;
}
