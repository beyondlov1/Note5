package com.beyond.note5.view.markdown.render;

import com.beyond.note5.view.markdown.render.bean.Line;

import java.util.List;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public interface LineSplitter extends MarkdownLifecycle{
    List<Line> split(String source);

    boolean shouldInsertSeparator(Line line);
    boolean shouldAppendSeparator(Line line);

    String getSeparator();
}
