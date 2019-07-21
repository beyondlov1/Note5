package com.beyond.note5.view.markdown.render;

import android.text.Spannable;

import com.beyond.note5.view.markdown.render.bean.Line;
import com.beyond.note5.view.markdown.render.resolver.LineResolver;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public interface LineProcessor extends TextSizeAware {
    void addResolver(LineResolver lineResolver);
    Spannable process(Line line);
}
