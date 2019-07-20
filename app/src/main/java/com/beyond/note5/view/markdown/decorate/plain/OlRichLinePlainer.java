package com.beyond.note5.view.markdown.decorate.plain;

import android.text.ParcelableSpan;

import com.beyond.note5.view.markdown.decorate.bean.RichLine;
import com.beyond.note5.view.markdown.decorate.bean.RichListLine;
import com.beyond.note5.view.markdown.decorate.span.OlMarkdownBulletSpan2;

/**
 * @author: beyond
 * @date: 2019/7/19
 */

public class OlRichLinePlainer extends AbstractRichLinePlainer {

    @Override
    protected String getTagForPlain(RichLine richLine) {
        if (richLine instanceof RichListLine){
            return (((RichListLine) richLine).getListIndex()+1)+". ";
        }
        throw new RuntimeException("类型不是richLine");
    }

    public static boolean isListLine(RichLine richLine){
        boolean found = false;
        ParcelableSpan[] spans = richLine.getSpans(ParcelableSpan.class);
        if (spans == null){
            return false;
        }
        for (ParcelableSpan parcelableSpan : spans) {
            if (parcelableSpan.getClass().equals(OlMarkdownBulletSpan2.class)) {
                found = true;
            }
        }
        return found;
    }
}
