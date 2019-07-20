package com.beyond.note5.view.markdown.decorate.plain;

import android.text.ParcelableSpan;

import com.beyond.note5.view.markdown.decorate.RichLineContext;
import com.beyond.note5.view.markdown.decorate.bean.RichLine;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public abstract class AbstractRichLinePlainer implements RichLinePlainer {

    protected RichLineContext context;

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
        return context.getTag();
    }

    protected Class getSpanClass(){
       return context.getSpanClass();
    }

    @Override
    public void setContext(RichLineContext context) {
        this.context = context;
    }
}
