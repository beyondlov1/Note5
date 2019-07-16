package com.beyond.note5.view.markdown.span.resolver;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public class H2LineResolver extends AbstractHLineResolver {
    @Override
    protected String getTag() {
        return H2;
    }

    @Override
    protected int getTextSize() {
        return (int)(baseTextSize*1.4);
    }
}
