package com.beyond.note5.view.markdown.render;

import android.text.SpannableStringBuilder;

import com.beyond.note5.view.markdown.render.resolver.LineResolver;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public interface MarkdownRender extends TextSizeAware {
    SpannableStringBuilder render(String source);
    void addResolver(LineResolver resolver);
}
