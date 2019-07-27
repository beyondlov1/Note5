package com.beyond.note5.predict.params;

public interface TagSource<T> {
    T getContent();
    void setContent(String newContent);
}
