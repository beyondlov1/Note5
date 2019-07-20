package com.beyond.note5.view.markdown.decorate.resolver;

import com.beyond.note5.view.markdown.decorate.bean.RichLine;
import com.beyond.note5.view.markdown.decorate.span.UlMarkdownBulletSpan2;

/**
 * @author: beyond
 * @date: 2019/7/19
 */

public class UlRichLineResolver extends AbstractRichLineResolver {

    @Override
    protected Object getSpanForResolve(RichLine line) {
        return new UlMarkdownBulletSpan2();
    }

}
