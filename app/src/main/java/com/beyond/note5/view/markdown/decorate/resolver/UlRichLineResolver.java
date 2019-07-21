package com.beyond.note5.view.markdown.decorate.resolver;

import android.text.ParcelableSpan;

import com.beyond.note5.view.markdown.decorate.bean.RichLine;
import com.beyond.note5.view.markdown.decorate.bean.RichListLine;
import com.beyond.note5.view.markdown.decorate.span.UlMarkdownBulletSpan2;

/**
 * @author: beyond
 * @date: 2019/7/19
 */

public class UlRichLineResolver extends AbstractRichLineResolver {

    @Override
    public boolean supportResolve(RichLine line) {
        boolean resolved = UlRichLineResolver.isListLine(line);
        return  RichListLine.isUlListLine(line.getContent()) && !resolved;
    }

    @Override
    protected Object getSpanForResolve(RichLine line) {
        return new UlMarkdownBulletSpan2();
    }

    public static boolean isListLine(RichLine richLine){
        boolean found = false;
        ParcelableSpan[] spans = richLine.getSpans(ParcelableSpan.class);
        if (spans == null){
            return false;
        }
        for (ParcelableSpan parcelableSpan : spans) {
            if (parcelableSpan.getClass().equals(UlMarkdownBulletSpan2.class)) {
                found = true;
                break;
            }
        }
        return found;
    }
}
