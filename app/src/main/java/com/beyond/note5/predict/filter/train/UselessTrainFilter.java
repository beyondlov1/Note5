package com.beyond.note5.predict.filter.train;

import com.beyond.note5.predict.params.TagSource;

public class UselessTrainFilter implements TrainFilter {

    private static final String[] USELESS_WORDS = new String[]{
            "的","是"
    };

    @Override
    public void doFilter(TagSource<String> tagSource) {
        String content = tagSource.getContent();
        for (String uselessWord : USELESS_WORDS) {
            content = content.replace(uselessWord, "");
        }
        tagSource.setContent(content);
    }
}
