package com.beyond.note5.view.markdown.decorate.span;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.style.TextAppearanceSpan;

import static android.graphics.Typeface.BOLD;

public class H3TextAppearanceSpan extends TextAppearanceSpan {

    public H3TextAppearanceSpan(String family, int style, int size, ColorStateList color, ColorStateList linkColor) {
        super(family, style, size, color, linkColor);
    }

    public H3TextAppearanceSpan(int textSize) {
        super("serif", BOLD, textSize,
                ColorStateList.valueOf(Color.BLACK), ColorStateList.valueOf(Color.RED));
    }
}