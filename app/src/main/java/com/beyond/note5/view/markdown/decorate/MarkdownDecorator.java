package com.beyond.note5.view.markdown.decorate;

import android.text.Editable;

import com.beyond.note5.view.markdown.decorate.plain.RichLinePlainer;
import com.beyond.note5.view.markdown.decorate.resolver.RichLineResolver;

/**
 * @author: beyond
 * @date: 2019/7/20
 */

public interface MarkdownDecorator {
    String H1 = "# ";
    String H2 = "## ";
    String H3 = "### ";
    String H4 = "#### ";
    String H5 = "##### ";
    String H6 = "###### ";
    String UL = "- ";
    String OL = "1.";

    void decorate(Editable editable);

    void decorate(Editable editable, boolean delete);

    String plain(Editable editable);

    void register(String tag, Class span, RichLineResolver resolver, RichLinePlainer plainer);
}
