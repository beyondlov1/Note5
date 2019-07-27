package com.beyond.note5.predict.filter.train;

import com.beyond.note5.predict.params.TagSource;
import com.beyond.note5.utils.TimeNLPUtil;

public class TimeExpressionTrainFilter implements TrainFilter {
    @Override
    public void doFilter(TagSource<String> tagSource) {
        String content = tagSource.getContent();
        String originTimeExpression = TimeNLPUtil.getOriginTimeExpression(content);
        tagSource.setContent(originTimeExpression);
    }
}
