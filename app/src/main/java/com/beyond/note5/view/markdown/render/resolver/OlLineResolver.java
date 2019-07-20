package com.beyond.note5.view.markdown.render.resolver;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import com.beyond.note5.view.markdown.render.bean.Line;
import com.beyond.note5.view.markdown.render.bean.ListLine;
import com.beyond.note5.view.markdown.render.resolver.span.MarkdownBulletSpan2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public class OlLineResolver implements LineResolver {

    private Pattern pattern;

    private int baseTextSize;

    @Override
    public boolean support(Line line) {
        return line instanceof ListLine;
    }

    @Override
    public Spannable resolveLine(Line line) {
        String tag = getTag(line);
        Spannable text = new SpannableStringBuilder(line.getContentWithoutTag(tag));
        int start = line.getTagStart(tag);
        int end = line.getContentWithoutTag(tag).length();
        text.setSpan(
                getSpan(line),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return text;
    }

    protected Object getSpan(Line line) {
        return new MarkdownBulletSpan2(0, Color.DKGRAY, ((ListLine) line).getListIndex()+1,20,30);
    }

    protected String getTag(Line line) {
        Matcher matcher = pattern.matcher(line.getSource());
        String group = "";
        while (matcher.find()) {
            group = matcher.group();
        }
        return group;
    }

    @Override
    public void init() {
        pattern = Pattern.compile("(\\d+\\.)");
    }

    @Override
    public void setBaseTextSize(int textSize) {
        baseTextSize = textSize;
    }
}
