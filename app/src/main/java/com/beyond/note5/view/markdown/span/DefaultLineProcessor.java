package com.beyond.note5.view.markdown.span;

import android.text.Spannable;
import android.text.SpannableString;

import com.beyond.note5.view.markdown.span.bean.Line;
import com.beyond.note5.view.markdown.span.resolver.H1LineResolver;
import com.beyond.note5.view.markdown.span.resolver.H2LineResolver;
import com.beyond.note5.view.markdown.span.resolver.H3LineResolver;
import com.beyond.note5.view.markdown.span.resolver.LineResolver;
import com.beyond.note5.view.markdown.span.resolver.OlLineResolver;
import com.beyond.note5.view.markdown.span.resolver.UlLineResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public class DefaultLineProcessor implements LineProcessor {

    private List<LineResolver> lineResolvers;

    private int baseTextSize;

    @Override
    public void addResolver(LineResolver lineResolver) {
        lineResolvers.add(lineResolver);
    }

    @Override
    public void setBaseTextSize(int textSize) {
        baseTextSize = textSize;
    }

    @Override
    public Spannable process(Line line) {
        Spannable resolvedSpannable = null;
        for (LineResolver lineResolver : lineResolvers) {
            if (lineResolver.support(line)){
                lineResolver.setBaseTextSize(baseTextSize);
                resolvedSpannable = lineResolver.resolveLine(line);
                break;
            }
        }
        if (resolvedSpannable == null){
            resolvedSpannable = new SpannableString(line.getSource());
        }
        return resolvedSpannable;
    }

    @Override
    public void init() {
        lineResolvers = getDefaultLineResolvers();
        for (LineResolver lineResolver : lineResolvers) {
            lineResolver.init();
        }
    }

    public List<LineResolver> getDefaultLineResolvers() {
        List<LineResolver> defaultLineResolvers = new ArrayList<>();
        defaultLineResolvers.add(new H1LineResolver());
        defaultLineResolvers.add(new H2LineResolver());
        defaultLineResolvers.add(new H3LineResolver());
        defaultLineResolvers.add(new UlLineResolver());
        defaultLineResolvers.add(new OlLineResolver());
        return defaultLineResolvers;
    }
}
