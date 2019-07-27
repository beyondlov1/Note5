package com.beyond.note5.predict.train;

import com.beyond.note5.predict.bean.TagGraph;
import com.beyond.note5.predict.filter.Filter;
import com.beyond.note5.predict.filter.FilterChain;

@SuppressWarnings("WeakerAccess")
public class FilterableTrainer implements Trainer {

    private final Trainer delegate;

    private FilterChain<String> filterChain;


    public FilterableTrainer(Trainer delegate) {
        this.delegate = delegate;
        this.filterChain = new FilterChain<>();
    }

    @Override
    public void trainSync(TrainSource source) throws Exception {
        filterChain.doFilter(source);
        delegate.trainSync(source);
    }

    @Override
    public void trainAsync(TrainSource source) throws Exception {
        delegate.trainAsync(source);
    }

    @Override
    public TagGraph getTagGraph() {
        return delegate.getTagGraph();
    }

    public void addFilter(Filter<String> filter) {
        filterChain.addFilter(filter);
    }
}
