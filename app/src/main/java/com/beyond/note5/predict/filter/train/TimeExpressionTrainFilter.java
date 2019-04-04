package com.beyond.note5.predict.filter.train;

import com.beyond.note5.predict.filter.Target;
import com.beyond.note5.utils.TimeNLPUtil;

public class TimeExpressionTrainFilter extends AbstractTrainFilter {
    @Override
    public void doFilter(Target<String> target) {
        String content = target.getTarget();
        String originTimeExpression = TimeNLPUtil.getOriginTimeExpression(content);
        target.setTarget(originTimeExpression);
    }
}
