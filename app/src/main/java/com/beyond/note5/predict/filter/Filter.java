package com.beyond.note5.predict.filter;

public interface Filter<T> {
    void doFilter(Target<T> target);
}
