package com.beyond.note5.view.markdown.render2;

/**
 * @author: beyond
 * @date: 2019/7/22
 */

public interface LineResolveStrategy {
    void getResolvers();
    void register(LineResolver resolver);
}
