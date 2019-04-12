package com.beyond.note5.predict.train;

import com.beyond.note5.predict.bean.TagGraph;
import com.beyond.note5.predict.train.filter.AbstractTrainTagFilter;
import com.beyond.note5.predict.train.target.TrainTagTarget;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class FilterableTagTrainer implements TagTrainer {

    private final TagTrainer tagTrainer;

    private List<AbstractTrainTagFilter> filters;


    public FilterableTagTrainer(TagTrainer tagTrainer) {
        this.tagTrainer = tagTrainer;
    }

    @Override
    public void trainSync(TrainTagTarget target) throws Exception {

        for (AbstractTrainTagFilter filter : filters) {
            filter.doFilter(target);
        }
        tagTrainer.trainSync(target);
    }

    @Override
    public void trainAsync(TrainTagTarget target) throws Exception {
        tagTrainer.trainAsync(target);
    }

    @Override
    public TagGraph getTagGraph() {
        return tagTrainer.getTagGraph();
    }

    public List<AbstractTrainTagFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<AbstractTrainTagFilter> filters) {
        this.filters = filters;
    }

    public void addFilter(AbstractTrainTagFilter filter) {
        if (filters == null) {
            filters = new LinkedList<>();
        }
        filters.add(filter);
    }

    public void removeFilter(AbstractTrainTagFilter filter){
        if (filters != null) {
            filters.add(filter);
        }
    }


}
