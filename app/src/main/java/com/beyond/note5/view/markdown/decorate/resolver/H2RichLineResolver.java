package com.beyond.note5.view.markdown.decorate.resolver;

import com.beyond.note5.view.markdown.decorate.bean.RichLine;
import com.beyond.note5.view.markdown.decorate.span.H2TextAppearanceSpan;

/**
 * @author: beyond
 * @date: 2019/7/19
 */

public class H2RichLineResolver extends AbstractHRichLineResolver {
    protected int getTextSize() {
        return (int)(baseTextSize*1.4);
    }

    @Override
    protected Object getSpanForResolve(RichLine line) {
        return new H2TextAppearanceSpan(getTextSize());
    }


}
