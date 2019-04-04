package com.beyond.note5.predict;

import com.beyond.note5.predict.bean.TagGraph;
import com.beyond.note5.predict.filter.target.TrainTarget;
import com.beyond.note5.predict.filter.train.AbstractTrainFilter;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class FilterableTagTrainer implements TagTrainer {

    private final TagTrainer tagTrainer;

    private List<AbstractTrainFilter> filters;


    public FilterableTagTrainer(TagTrainer tagTrainer) {
        this.tagTrainer = tagTrainer;
    }

    @Override
    public void trainSync(TrainTarget target) throws Exception {

        for (AbstractTrainFilter filter : filters) {
            filter.doFilter(target);
        }
        tagTrainer.trainSync(target);
    }

    @Override
    public TagGraph getTagGraph() {
        return tagTrainer.getTagGraph();
    }

    public List<AbstractTrainFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<AbstractTrainFilter> filters) {
        this.filters = filters;
    }

    public void addFilter(AbstractTrainFilter filter) {
        if (filters == null) {
            filters = new LinkedList<>();
        }
        filters.add(filter);
    }

    public void removeFilter(AbstractTrainFilter filter){
        if (filters != null) {
            filters.add(filter);
        }
    }


}
