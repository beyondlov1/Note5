package com.beyond.note5.view.markdown.span.resolver;

import android.graphics.Color;

import com.beyond.note5.view.markdown.span.resolver.span.MarkdownBulletSpan2;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public class UlLineResolver extends AbstractLineResolver {
    protected Object getSpan() {
        return  new MarkdownBulletSpan2(0, Color.DKGRAY, 0,20,10);
    }

    protected String getTag() {
        return UL;
    }
}
