package com.beyond.note5.view.markdown.decorate;

import com.beyond.note5.view.markdown.span.TextSizeAware;

public interface RichLineResolver extends TextSizeAware {

    String H1 = "# ";
    String H2 = "## ";
    String H3= "### ";
    String H4 = "#### ";
    String H5 = "##### ";
    String H6= "###### ";
    String UL= "- ";

    boolean supportResolve(RichLine richLine);
    void resolve(RichLine richLine);
    boolean supportPlain(RichLine richLine);
    String plain(RichLine richLine);
}