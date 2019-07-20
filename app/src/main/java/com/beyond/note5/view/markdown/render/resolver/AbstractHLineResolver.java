package com.beyond.note5.view.markdown.render.resolver;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.style.TextAppearanceSpan;

import static android.graphics.Typeface.NORMAL;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public abstract class AbstractHLineResolver extends AbstractLineResolver {

    @Override
    protected Object getSpan() {
        return new TextAppearanceSpan(
                "serif", NORMAL, getTextSize(),
                ColorStateList.valueOf(Color.DKGRAY), ColorStateList.valueOf(Color.RED));
    }

    protected abstract int getTextSize() ;
}
