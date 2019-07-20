package com.beyond.note5.view.markdown.decorate.span;

import android.graphics.Color;

import com.beyond.note5.view.markdown.render.resolver.span.MarkdownBulletSpan2;

public class UlMarkdownBulletSpan2 extends MarkdownBulletSpan2 {

    public UlMarkdownBulletSpan2(int level, int color, int pointIndex, int mGapWidth, int tab) {
        super(level, color, pointIndex, mGapWidth, tab);
    }

    public UlMarkdownBulletSpan2() {
        super(0, Color.DKGRAY, 0, 20, 10);
    }
}