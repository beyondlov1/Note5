package com.beyond.note5.predict.filter.train;

import com.beyond.note5.predict.filter.Filter;
import com.beyond.note5.predict.filter.Target;

public abstract class AbstractTrainFilter implements Filter<String> {
    @Override
    public abstract void doFilter(Target<String> target);
}
