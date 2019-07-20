package com.beyond.note5.view.markdown.render.resolver;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public class H1LineResolver extends AbstractHLineResolver {
    @Override
    protected String getTag() {
        return H1;
    }

    @Override
    protected int getTextSize() {
        return (int)(baseTextSize*1.5);
    }
}
