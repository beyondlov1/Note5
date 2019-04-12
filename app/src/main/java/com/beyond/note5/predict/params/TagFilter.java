package com.beyond.note5.predict.params;

public interface TagFilter<T> {
    void doFilter(TagTarget<T> tagTarget);
}
