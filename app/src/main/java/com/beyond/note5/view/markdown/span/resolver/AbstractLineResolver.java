package com.beyond.note5.view.markdown.span.resolver;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;

import com.beyond.note5.view.markdown.span.bean.Line;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public abstract class AbstractLineResolver implements LineResolver {

    protected int baseTextSize;

    @Override
    public void init() {

    }

    @Override
    public void setBaseTextSize(int baseTextSize) {
        this.baseTextSize = baseTextSize;
    }

    @Override
    public boolean support(Line line) {
        return line.startWith(getTag());
    }

    @Override
    public Spannable resolveLine(Line line) {
        Spannable text = new SpannableString(line.getContentWithoutTag(getTag()));
        int start = line.getTagStart(getTag());
        int end = line.getContentWithoutTag(getTag()).length();
        text.setSpan(
                getSpan(),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return text;
    }

    protected abstract String getTag();

    protected abstract Object getSpan();
}
