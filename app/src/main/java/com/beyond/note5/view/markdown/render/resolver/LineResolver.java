package com.beyond.note5.view.markdown.render.resolver;

import android.text.Spannable;

import com.beyond.note5.view.markdown.render.TextSizeAware;
import com.beyond.note5.view.markdown.render.bean.Line;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public interface LineResolver extends TextSizeAware {

    String H1 = "# ";
    String H2 = "## ";
    String H3= "### ";
    String H4 = "#### ";
    String H5 = "##### ";
    String H6= "###### ";
    String UL= "- ";
    
    boolean support(Line line);
    Spannable resolveLine(Line line);
}
