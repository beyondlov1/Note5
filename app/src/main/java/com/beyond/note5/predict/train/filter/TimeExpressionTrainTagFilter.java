package com.beyond.note5.predict.train.filter;

import com.beyond.note5.predict.params.TagTarget;
import com.beyond.note5.utils.TimeNLPUtil;

public class TimeExpressionTrainTagFilter extends AbstractTrainTagFilter {
    @Override
    public void doFilter(TagTarget<String> tagTarget) {
        String content = tagTarget.getTarget();
        String originTimeExpression = TimeNLPUtil.getOriginTimeExpression(content);
        tagTarget.setTarget(originTimeExpression);
    }
}
