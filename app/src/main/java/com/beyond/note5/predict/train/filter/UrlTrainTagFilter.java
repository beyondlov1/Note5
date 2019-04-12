package com.beyond.note5.predict.train.filter;

import com.beyond.note5.predict.params.TagTarget;
import com.beyond.note5.utils.WebViewUtil;

import org.apache.commons.lang3.StringUtils;

public class UrlTrainTagFilter extends AbstractTrainTagFilter {
    @Override
    public void doFilter(TagTarget<String> tagTarget) {
        String content = tagTarget.getTarget();
        String url = WebViewUtil.getUrlOrSearchUrl(content);
        if (StringUtils.isNotBlank(url)){
            String newContent = content.replace(url, "");
            tagTarget.setTarget(newContent);
        }
    }
}
