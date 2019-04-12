package com.beyond.note5.predict.train.filter;

import com.beyond.note5.predict.params.TagFilter;
import com.beyond.note5.predict.params.TagTarget;

public abstract class AbstractTrainTagFilter implements TagFilter<String> {
    @Override
    public abstract void doFilter(TagTarget<String> tagTarget);
}
