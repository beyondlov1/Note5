package com.beyond.note5.view.markdown.span.resolver;

import android.text.Spannable;

import com.beyond.note5.view.markdown.span.TextSizeAware;
import com.beyond.note5.view.markdown.span.MarkdownLifecycle;
import com.beyond.note5.view.markdown.span.bean.Line;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public interface LineResolver extends MarkdownLifecycle,TextSizeAware {

    public static final String H1 = "# ";
    public static final String H2 = "## ";
    public static final String H3= "### ";
    public static final String H4 = "#### ";
    public static final String H5 = "##### ";
    public static final String H6= "###### ";
    public static final String UL= "- ";
    
    boolean support(Line line);
    Spannable resolveLine(Line line);
}
