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

public class H1RichLineResolver extends AbstractHRichLineResolver {
    @Override
    protected int getTextSize() {
        return (int)(baseTextSize*1.5);
    }

    @Override
    protected Class getSpanClass() {
        return H1TextAppearanceSpan.class;
    }

    @Override
    protected Object getSpanForResolve(RichLine line) {
        return new H1TextAppearanceSpan(
                "serif", NORMAL, getTextSize(),
                ColorStateList.valueOf(Color.DKGRAY), ColorStateList.valueOf(Color.RED));
    }

    @Override
    protected String getTagForResolve(RichLine line) {
        return H1;
    }

    class H1TextAppearanceSpan extends TextAppearanceSpan {

        public H1TextAppearanceSpan(String family, int style, int size, ColorStateList color, ColorStateList linkColor) {
            super(family, style, size, color, linkColor);
        }
    }
}
