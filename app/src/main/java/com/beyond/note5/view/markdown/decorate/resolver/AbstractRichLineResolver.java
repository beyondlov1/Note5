package com.beyond.note5.view.markdown.decorate.resolver;

import android.text.Spanned;

import com.beyond.note5.view.markdown.decorate.RichLineContext;
import com.beyond.note5.view.markdown.decorate.bean.RichLine;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public abstract class AbstractRichLineResolver implements RichLineResolver {

    protected int baseTextSize;

    protected RichLineContext context;

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

    protected void afterSpanSet(RichLine line) {
        line.deleteTag(getTagForResolve(line));
    }

    protected String getTagForResolve(RichLine line){
        return context.getTag();
    }

    @Override
    public void setBaseTextSize(int baseTextSize) {
        this.baseTextSize = baseTextSize;
    }

    @Override
    public void setContext(RichLineContext context) {
        this.context = context;
    }
}
