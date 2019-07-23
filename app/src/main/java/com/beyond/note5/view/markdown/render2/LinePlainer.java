package com.beyond.note5.view.markdown.render2;

/**
 * @author: beyond
 * @date: 2019/7/23
 */

public interface LinePlainer {
    boolean support(Line line);
    String plain(Line line);
}
