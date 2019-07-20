package com.beyond.note5.view.markdown.render;

import android.text.SpannableStringBuilder;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public interface MarkdownRender extends MarkdownLifecycle,TextSizeAware {
    SpannableStringBuilder render(String source);
}
