package com.beyond.note5.predict.filter;

import com.beyond.note5.predict.params.TagSource;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: beyond
 * @date: 2019/7/27
 */

public class FilterChain<T> {
    private List<Filter<T>> filters;

    public FilterChain() {
        filters = new ArrayList<>();
    }

    public void doFilter(TagSource<T> source) {
        for (Filter<T> filter : filters) {
            filter.doFilter(source);
        }
    }

    public void addFilter(Filter<T> filter) {
        if (filters == null) {
            filters = new LinkedList<>();
        }
        filters.add(filter);
    }

}
