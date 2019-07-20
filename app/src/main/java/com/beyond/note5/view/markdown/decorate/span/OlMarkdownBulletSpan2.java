package com.beyond.note5.view.markdown.decorate.span;

import android.graphics.Color;

import com.beyond.note5.view.markdown.decorate.bean.RichLine;
import com.beyond.note5.view.markdown.decorate.bean.RichListLine;
import com.beyond.note5.view.markdown.render.resolver.span.MarkdownBulletSpan2;

public class OlMarkdownBulletSpan2 extends MarkdownBulletSpan2 {

        public OlMarkdownBulletSpan2(int level, int color, int pointIndex, int mGapWidth, int tab) {
            super(level, color, pointIndex, mGapWidth, tab);
        }

    public OlMarkdownBulletSpan2(RichLine line) {
        super(0, Color.DKGRAY, ((RichListLine) line).getListIndex()+1,20,30);
    }
}