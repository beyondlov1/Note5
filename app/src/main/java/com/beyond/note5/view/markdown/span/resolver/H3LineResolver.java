package com.beyond.note5.view.markdown.span.resolver;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public class H3LineResolver extends AbstractHLineResolver {
    @Override
    protected String getTag() {
        return H3;
    }

    @Override
    protected int getTextSize() {
        return (int)(baseTextSize*1.3);
    }
}
