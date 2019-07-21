package com.beyond.note5.view.markdown.decorate.resolver.init;

import com.beyond.note5.view.markdown.decorate.span.H2TextAppearanceSpan;
import com.beyond.note5.view.markdown.render.resolver.AbstractHLineResolver;

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

    @Override
    protected Object getSpan() {
        return new H2TextAppearanceSpan(getTextSize());
    }
}
