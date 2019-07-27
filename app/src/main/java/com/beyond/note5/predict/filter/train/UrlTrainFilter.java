package com.beyond.note5.predict.filter.train;

import com.beyond.note5.predict.params.TagSource;
import com.beyond.note5.utils.WebViewUtil;

import org.apache.commons.lang3.StringUtils;

public class UrlTrainFilter implements TrainFilter {
    @Override
    public void doFilter(TagSource<String> tagSource) {
        String content = tagSource.getContent();
        String url = WebViewUtil.getUrlOrSearchUrl(content);
        if (StringUtils.isNotBlank(url)){
            String newContent = content.replace(url, "");
            tagSource.setContent(newContent);
        }
    }
}
