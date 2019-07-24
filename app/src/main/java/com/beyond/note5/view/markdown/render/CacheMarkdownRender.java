package com.beyond.note5.view.markdown.render;

import android.text.SpannableStringBuilder;

import com.beyond.note5.view.markdown.render.resolver.LineResolver;

import java.util.LinkedHashMap;

/**
 * @author: beyond
 * @date: 2019/7/24
 */

public class CacheMarkdownRender implements MarkdownRender{

    private int maxCacheSize = 500;

    private MarkdownRender delegate;

    private int textSize;

    private LinkedHashMap<CacheKey,SpannableStringBuilder> cache;

    public CacheMarkdownRender(MarkdownRender delegate) {
        this.delegate = delegate;
    }

    public CacheMarkdownRender(MarkdownRender delegate, int maxCacheSize) {
        this.delegate = delegate;
        this.maxCacheSize = maxCacheSize;
    }

    @Override
    public void setBaseTextSize(int textSize) {
        delegate.setBaseTextSize(textSize);
        this.textSize = textSize;
        //LRU策略
        this.cache = new LinkedHashMap<CacheKey,SpannableStringBuilder>(32,0.75f,true){
            @Override
            protected boolean removeEldestEntry(Entry eldest) {
                return size() > maxCacheSize;
            }
        };
    }

    @Override
    public SpannableStringBuilder render(String source) {
        SpannableStringBuilder renderedText = getFromCache(source,textSize);
        if (renderedText != null){
            return renderedText;
        }
        renderedText = delegate.render(source);
        cache.put(new CacheKey(source,textSize),renderedText);
        return renderedText;
    }

    @Override
    public void addResolver(LineResolver resolver) {
        delegate.addResolver(resolver);
    }

    private SpannableStringBuilder getFromCache(String source, int textSize) {
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
