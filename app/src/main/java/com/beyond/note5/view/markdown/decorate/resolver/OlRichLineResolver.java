package com.beyond.note5.view.markdown.decorate.resolver;

import android.text.ParcelableSpan;

import com.beyond.note5.view.markdown.decorate.bean.RichLine;
import com.beyond.note5.view.markdown.decorate.bean.RichListLine;
import com.beyond.note5.view.markdown.decorate.span.OlMarkdownBulletSpan2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: beyond
 * @date: 2019/7/19
 */

public class OlRichLineResolver extends AbstractRichLineResolver {

    private Pattern pattern = Pattern.compile("(\\d+\\. )");
    @Override
    public boolean supportResolve(RichLine line) {
        boolean resolved = OlRichLineResolver.isListLine(line);
        return  RichListLine.isOlListLine(line.getContent()) && !resolved;
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
        return new OlMarkdownBulletSpan2(line);
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
                break;
            }
        }
        return found;
    }


}
