package com.beyond.note5.view.markdown.decorate.plain;

import com.beyond.note5.view.markdown.decorate.RichLineContext;
import com.beyond.note5.view.markdown.decorate.bean.RichLine;

/**
 * @author: beyond
 * @date: 2019/7/20
 */

public interface RichLinePlainer {
    boolean supportPlain(RichLine richLine);
    String plain(RichLine richLine);
    void setContext(RichLineContext richLineContext);
}
