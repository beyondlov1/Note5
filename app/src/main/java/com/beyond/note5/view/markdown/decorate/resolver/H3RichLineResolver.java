package com.beyond.note5.view.markdown.decorate.resolver;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.style.TextAppearanceSpan;

import com.beyond.note5.view.markdown.decorate.RichLine;

import static android.graphics.Typeface.NORMAL;

/**
 * @author: beyond
 * @date: 2019/7/19
 */

public class H3RichLineResolver extends AbstractHRichLineResolver {
    @Override
    protected int getTextSize() {
        return (int)(baseTextSize*1.5);
    }

    @Override
    protected String getTagForResolve(RichLine line) {
        return H3;
    }

    @Override
    protected Class getSpanClass() {
        return H3TextAppearanceSpan.class;
    }

    @Override
    protected Object getSpanForResolve(RichLine line) {
        return new H3TextAppearanceSpan(
                "serif", NORMAL, getTextSize(),
                ColorStateList.valueOf(Color.DKGRAY), ColorStateList.valueOf(Color.RED));
    }

    class H3TextAppearanceSpan extends TextAppearanceSpan {

        public H3TextAppearanceSpan(String family, int style, int size, ColorStateList color, ColorStateList linkColor) {
            super(family, style, size, color, linkColor);
        }
    }
}
