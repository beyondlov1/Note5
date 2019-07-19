package com.beyond.note5.view.markdown.decorate.resolver;

import android.text.ParcelableSpan;
import android.text.Spanned;

import com.beyond.note5.view.markdown.decorate.RichLine;
import com.beyond.note5.view.markdown.decorate.RichLineResolver;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public abstract class AbstractRichLineResolver implements RichLineResolver {

    protected int baseTextSize;

    protected Object span;

    @Override
    public boolean supportResolve(RichLine line) {
        return line.startsWith(getTagForResolve(line));
    }

    @Override
    public void resolve(RichLine line) {
        int start = line.getStart();
        int end = line.getEnd();
        line.getFullSource().setSpan(
                getSpanForResolve(line),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        afterSpanSet(line);
    }

    protected abstract Object getSpanForResolve(RichLine line);

    protected abstract Class getSpanClass();

    @Override
    public void setBaseTextSize(int baseTextSize) {
        this.baseTextSize = baseTextSize;
    }

    protected abstract String getTagForResolve(RichLine line);

    protected void afterSpanSet(RichLine line) {
        line.deleteTag(getTagForResolve(line));
    }

    @Override
    public boolean supportPlain(RichLine richLine) {
        boolean found = false;
        ParcelableSpan[] spans = richLine.getSpans(ParcelableSpan.class);
        if (spans == null){
            return false;
        }
        for (ParcelableSpan parcelableSpan : spans) {
            if (parcelableSpan.getClass().equals(getSpanClass())) {
                found = true;
            }
        }
        return found;
    }

    @Override
    public String plain(RichLine richLine) {
        if (supportPlain(richLine)) {
            return getTagForPlain(richLine) + richLine.getContent();
        }
        return richLine.getContent();
    }

    protected String getTagForPlain(RichLine richLine){
        return getTagForResolve(richLine);
    }
}
