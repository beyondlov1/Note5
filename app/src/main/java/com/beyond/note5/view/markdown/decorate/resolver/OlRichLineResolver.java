package com.beyond.note5.view.markdown.decorate.resolver;

import android.graphics.Color;
import android.text.ParcelableSpan;

import com.beyond.note5.view.markdown.decorate.RichLine;
import com.beyond.note5.view.markdown.decorate.RichListLine;
import com.beyond.note5.view.markdown.span.resolver.span.MarkdownBulletSpan2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: beyond
 * @date: 2019/7/19
 */

public class OlRichLineResolver extends AbstractRichLineResolver {

    private Pattern pattern = Pattern.compile("(\\d+\\.)");
    @Override
    public boolean supportResolve(RichLine line) {
        return line instanceof RichListLine;
    }

    @Override
    protected String getTagForResolve(RichLine line) {
        Matcher matcher = pattern.matcher(line.getContent());
        String group = "";
        while (matcher.find()) {
            group = matcher.group();
        }
        return group;
    }

    @Override
    protected Object getSpanForResolve(RichLine line) {
        return new OlMarkdownBulletSpan2(0, Color.DKGRAY, ((RichListLine) line).getListIndex()+1,20,30);
    }

    @Override
    protected String getTagForPlain(RichLine richLine) {
        if (richLine instanceof RichListLine){
            return (((RichListLine) richLine).getListIndex()+1)+". ";
        }
        return super.getTagForPlain(richLine);
    }

    @Override
    protected Class getSpanClass() {
        return OlMarkdownBulletSpan2.class;
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

    class OlMarkdownBulletSpan2 extends MarkdownBulletSpan2 {

        public OlMarkdownBulletSpan2(int level, int color, int pointIndex, int mGapWidth, int tab) {
            super(level, color, pointIndex, mGapWidth, tab);
        }
    }
}
