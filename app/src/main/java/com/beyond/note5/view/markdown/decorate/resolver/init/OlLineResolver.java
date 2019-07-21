package com.beyond.note5.view.markdown.decorate.resolver.init;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import com.beyond.note5.view.markdown.decorate.span.OlMarkdownBulletSpan2;
import com.beyond.note5.view.markdown.render.bean.Line;
import com.beyond.note5.view.markdown.render.bean.ListLine;
import com.beyond.note5.view.markdown.render.resolver.LineResolver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public class OlLineResolver implements LineResolver {

    private Pattern pattern;

    private int baseTextSize;

    public OlLineResolver() {
        pattern = Pattern.compile("(\\d+\\.)");
    }

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
        if (text.length() == 0){
            text = new SpannableStringBuilder(line.getSource());
        }
        return text;
    }

    protected Object getSpan(Line line) {
        return new OlMarkdownBulletSpan2(0, Color.DKGRAY, ((ListLine) line).getListIndex()+1,20,30);
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
    public void setBaseTextSize(int textSize) {
        baseTextSize = textSize;
    }
}
