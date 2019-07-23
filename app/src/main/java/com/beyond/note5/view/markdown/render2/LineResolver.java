package com.beyond.note5.view.markdown.render2;

import android.text.SpannableStringBuilder;

/**
 * @author: beyond
 * @date: 2019/7/22
 */

public interface LineResolver {
    boolean support(Line line);
    SpannableStringBuilder resolve(Line line);
    void setContext(LineContext context);
}
