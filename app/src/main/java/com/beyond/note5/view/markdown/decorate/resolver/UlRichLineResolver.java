package com.beyond.note5.view.markdown.decorate.resolver;

import android.graphics.Color;

import com.beyond.note5.view.markdown.decorate.RichLine;
import com.beyond.note5.view.markdown.span.resolver.span.MarkdownBulletSpan2;

/**
 * @author: beyond
 * @date: 2019/7/19
 */

public class UlRichLineResolver extends AbstractRichLineResolver {

    @Override
    protected String getTagForResolve(RichLine line) {
        return UL;
    }

    @Override
    protected Object getSpanForResolve(RichLine line) {
        return new UlMarkdownBulletSpan2(0, Color.DKGRAY, 0, 20, 10);
    }

    @Override
    protected Class getSpanClass() {
        return UlMarkdownBulletSpan2.class;
    }

    class UlMarkdownBulletSpan2 extends MarkdownBulletSpan2 {

        public UlMarkdownBulletSpan2(int level, int color, int pointIndex, int mGapWidth, int tab) {
            super(level, color, pointIndex, mGapWidth, tab);
        }
    }
}
