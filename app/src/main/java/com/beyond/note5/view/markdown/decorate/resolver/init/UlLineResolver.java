package com.beyond.note5.view.markdown.decorate.resolver.init;

import com.beyond.note5.view.markdown.decorate.span.UlMarkdownBulletSpan2;
import com.beyond.note5.view.markdown.render.resolver.AbstractLineResolver;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public class UlLineResolver extends AbstractLineResolver {
    protected Object getSpan() {
        return  new UlMarkdownBulletSpan2();
    }

    protected String getTag() {
        return UL;
    }
}
