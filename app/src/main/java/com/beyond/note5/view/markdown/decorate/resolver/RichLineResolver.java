package com.beyond.note5.view.markdown.decorate.resolver;

import com.beyond.note5.view.markdown.decorate.RichLineContext;
import com.beyond.note5.view.markdown.decorate.bean.RichLine;
import com.beyond.note5.view.markdown.render.TextSizeAware;

public interface RichLineResolver extends TextSizeAware {
    boolean supportResolve(RichLine richLine);
    void resolve(RichLine richLine);
    void setContext(RichLineContext richLineContext);
}