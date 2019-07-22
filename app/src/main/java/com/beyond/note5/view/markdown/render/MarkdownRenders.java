package com.beyond.note5.view.markdown.render;

import android.text.SpannableStringBuilder;
import android.util.SparseArray;
import android.widget.TextView;

import com.beyond.note5.view.custom.AutoSizeTextView;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public abstract class MarkdownRenders {
    private static final SparseArray<SpannableStringBuilder> cache = new SparseArray<>();
    private static  MarkdownRender markdownRender = MarkdownRenderHolder.INSTANCE;
    private static class MarkdownRenderHolder{
        static final MarkdownRender INSTANCE =  new DefaultMarkdownRender();
    }
    public static void render(TextView textView, String source){
        SpannableStringBuilder renderedText = getFromCache(source);
        if (renderedText != null){
            textView.setText(renderedText);
            return;
        }
        if (textView instanceof AutoSizeTextView){
            ((AutoSizeTextView) textView).computeAndSetTextSize(source);
        }
        markdownRender.setBaseTextSize((int)textView.getTextSize());
        renderedText = markdownRender.render(source);
        cache.put(source.hashCode(),renderedText);
        textView.setText(renderedText);
    }

    public static SpannableStringBuilder render(String source, int textSize){
        SpannableStringBuilder renderedText = getFromCache(source);
        if (renderedText != null){
            return renderedText;
        }
        markdownRender.setBaseTextSize(textSize);
        renderedText = markdownRender.render(source);
        cache.put(source.hashCode(),renderedText);
        return renderedText;
    }

    private static SpannableStringBuilder getFromCache( String source) {
        return cache.get(source.hashCode());
    }
}
