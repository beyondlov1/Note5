package com.beyond.note5.view.markdown.decorate.resolver.init;

import com.beyond.note5.view.markdown.decorate.span.H3TextAppearanceSpan;
import com.beyond.note5.view.markdown.render.resolver.AbstractHLineResolver;

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

    @Override
    protected Object getSpan() {
        return new H3TextAppearanceSpan(getTextSize());
    }
}
