package com.beyond.note5.view.markdown.decorate;

import android.text.Editable;

import com.beyond.note5.view.markdown.decorate.plain.RichLinePlainer;
import com.beyond.note5.view.markdown.decorate.resolver.RichLineResolver;

/**
 * @author: beyond
 * @date: 2019/7/20
 */

public interface MarkdownDecorator {
    void decorate(Editable editable);
    String plain(Editable editable);
    void register(String tag, Class span, RichLineResolver resolver, RichLinePlainer plainer);
}
