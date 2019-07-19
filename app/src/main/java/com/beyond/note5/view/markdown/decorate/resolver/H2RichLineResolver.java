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

public class H2RichLineResolver extends AbstractHRichLineResolver {
    @Override
    protected int getTextSize() {
        return (int)(baseTextSize*1.4);
    }

    @Override
    protected String getTagForResolve(RichLine line) {
        return H2;
    }

    @Override
    protected Class getSpanClass() {
        return H2TextAppearanceSpan.class;
    }

    @Override
    protected Object getSpanForResolve(RichLine line) {
        return new H2TextAppearanceSpan(
                "serif", NORMAL, getTextSize(),
                ColorStateList.valueOf(Color.DKGRAY), ColorStateList.valueOf(Color.RED));
    }

    class H2TextAppearanceSpan extends TextAppearanceSpan {

        public H2TextAppearanceSpan(String family, int style, int size, ColorStateList color, ColorStateList linkColor) {
            super(family, style, size, color, linkColor);
        }
    }
}
