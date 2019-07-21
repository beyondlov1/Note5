package com.beyond.note5.view.markdown.render;

import android.text.SpannableStringBuilder;

import com.beyond.note5.view.markdown.render.bean.Line;
import com.beyond.note5.view.markdown.render.resolver.LineResolver;

import java.util.List;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public class DefaultMarkdownRender implements MarkdownRender {

    private LineProcessor lineProcessor;

    private LineSplitter lineSplitter;

    public DefaultMarkdownRender() {
        lineProcessor = new DefaultLineProcessor();
        lineSplitter = new DefaultLineSplitter();
    }

    @Override
    public void setBaseTextSize(int textSize) {
        lineProcessor.setBaseTextSize(textSize);
    }

    @Override
    public SpannableStringBuilder render(String source) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        List<Line> lines = lineSplitter.split(source);
        for (Line line : lines) {
            if (lineSplitter.shouldInsertSeparator(line)){
                ssb.append(lineSplitter.getSeparator());
            }
            ssb.append(lineProcessor.process(line));
            if (lineSplitter.shouldAppendSeparator(line)){
                ssb.append(lineSplitter.getSeparator());
            }
        }
        return ssb;
    }

    @Override
    public void addResolver(LineResolver resolver) {
        lineProcessor.addResolver(resolver);
    }

}
