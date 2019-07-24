package com.beyond.note5.view.markdown.render;

import android.text.SpannableStringBuilder;
import android.widget.TextView;

import java.util.HashMap;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public abstract class MarkdownRenders {
    private static final HashMap<CacheKey,SpannableStringBuilder> cache = new HashMap<>();
    private static  MarkdownRender markdownRender = MarkdownRenderHolder.INSTANCE;
    private static class MarkdownRenderHolder{
        static final MarkdownRender INSTANCE =  new DefaultMarkdownRender();
    }
    public static void render(TextView textView, String source){
        SpannableStringBuilder renderedText = getFromCache(source, (int) textView.getTextSize());
        if (renderedText != null){
            textView.setText(renderedText);
            return;
        }
        float baseTextSize = textView.getTextSize();
        markdownRender.setBaseTextSize((int) baseTextSize);
        renderedText = markdownRender.render(source);
        cache.put(new CacheKey(source, (int) baseTextSize),renderedText);
        textView.setText(renderedText);
    }

    public static SpannableStringBuilder render(String source, int textSize){
        SpannableStringBuilder renderedText = getFromCache(source,textSize);
        if (renderedText != null){
            return renderedText;
        }
        markdownRender.setBaseTextSize(textSize);
        renderedText = markdownRender.render(source);
        cache.put(new CacheKey(source,textSize),renderedText);
        return renderedText;
    }

    private static SpannableStringBuilder getFromCache(String source, int textSize) {
        return cache.get(new CacheKey(source,textSize));
    }

    private static class CacheKey{
        private String source;
        private int textSize;

        CacheKey(String source, int textSize) {
            this.source = source;
            this.textSize = textSize;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheKey cacheKey = (CacheKey) o;

            if (textSize != cacheKey.textSize) return false;
            return source != null ? source.equals(cacheKey.source) : cacheKey.source == null;
        }

        @Override
        public int hashCode() {
            int result = source != null ? source.hashCode() : 0;
            result = 31 * result + textSize;
            return result;
        }
    }
}
