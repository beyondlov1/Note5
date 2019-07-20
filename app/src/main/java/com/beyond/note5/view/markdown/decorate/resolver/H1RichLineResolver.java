package com.beyond.note5.view.markdown.decorate.resolver;

import com.beyond.note5.view.markdown.decorate.bean.RichLine;
import com.beyond.note5.view.markdown.decorate.span.H1TextAppearanceSpan;

/**
 * @author: beyond
 * @date: 2019/7/19
 */

public class H1RichLineResolver extends AbstractHRichLineResolver {
    protected int getTextSize() {
        return (int)(baseTextSize*1.5);
    }

    @Override
    protected Object getSpanForResolve(RichLine line) {
        return new H1TextAppearanceSpan(getTextSize());
    }

}
