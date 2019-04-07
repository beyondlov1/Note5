package com.beyond.note5.predict.filter.train;

import com.beyond.note5.predict.filter.Target;
import com.beyond.note5.utils.WebViewUtil;

import org.apache.commons.lang3.StringUtils;

public class UrlTrainFilter extends AbstractTrainFilter {
    @Override
    public void doFilter(Target<String> target) {
        String content = target.getTarget();
        String url = WebViewUtil.getUrlOrSearchUrl(content);
        if (StringUtils.isNotBlank(url)){
            String newContent = content.replace(url, "");
            target.setTarget(newContent);
        }
    }
}
