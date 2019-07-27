package com.beyond.note5.predict.filter;

import com.beyond.note5.predict.params.TagSource;

public interface Filter<T> {
    void doFilter(TagSource<T> tagSource);
}
