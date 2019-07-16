package com.beyond.note5.view.markdown.span;

import android.text.Spannable;

import com.beyond.note5.view.markdown.span.bean.Line;
import com.beyond.note5.view.markdown.span.resolver.LineResolver;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public interface LineProcessor extends MarkdownLifecycle,TextSizeAware {
    void addResolver(LineResolver lineResolver);
    Spannable process(Line line);
}
