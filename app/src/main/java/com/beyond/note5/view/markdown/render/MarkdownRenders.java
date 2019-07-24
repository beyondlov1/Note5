package com.beyond.note5.view.markdown.render;

import android.text.SpannableStringBuilder;
import android.widget.TextView;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public abstract class MarkdownRenders {

    private static  MarkdownRender markdownRender = MarkdownRenderHolder.INSTANCE;
    private static class MarkdownRenderHolder{
        static final MarkdownRender INSTANCE =  new CacheMarkdownRender(new DefaultMarkdownRender());
    }

    public static void render(TextView textView, String source){
        float baseTextSize = textView.getTextSize();
        markdownRender.setBaseTextSize((int) baseTextSize);
        textView.setText(markdownRender.render(source));
    }

    public static SpannableStringBuilder render(String source, int textSize){
        markdownRender.setBaseTextSize(textSize);
        return markdownRender.render(source);
    }

}
