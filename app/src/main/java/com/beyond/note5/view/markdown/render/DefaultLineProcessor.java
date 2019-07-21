package com.beyond.note5.view.markdown.render;

import android.text.Spannable;
import android.text.SpannableString;

import com.beyond.note5.view.markdown.render.bean.Line;
import com.beyond.note5.view.markdown.render.resolver.H1LineResolver;
import com.beyond.note5.view.markdown.render.resolver.H2LineResolver;
import com.beyond.note5.view.markdown.render.resolver.H3LineResolver;
import com.beyond.note5.view.markdown.render.resolver.LineResolver;
import com.beyond.note5.view.markdown.render.resolver.OlLineResolver;
import com.beyond.note5.view.markdown.render.resolver.UlLineResolver;

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
        if (lineResolvers == null) {
            lineResolvers = new ArrayList<>();
        }
        lineResolvers.add(lineResolver);
    }

    @Override
    public void setBaseTextSize(int textSize) {
        baseTextSize = textSize;
    }

    @Override
    public Spannable process(Line line) {
        if (lineResolvers == null || lineResolvers.isEmpty()) {
            loadDefaultLineResolvers();
        }
        Spannable resolvedSpannable = null;
        for (LineResolver lineResolver : lineResolvers) {
            if (lineResolver.support(line)) {
                lineResolver.setBaseTextSize(baseTextSize);
                resolvedSpannable = lineResolver.resolveLine(line);
                break;
            }
        }
        if (resolvedSpannable == null) {
            resolvedSpannable = new SpannableString(line.getSource());
        }
        return resolvedSpannable;
    }

    private void loadDefaultLineResolvers() {
        addResolver(new H1LineResolver());
        addResolver(new H2LineResolver());
        addResolver(new H3LineResolver());
        addResolver(new UlLineResolver());
        addResolver(new OlLineResolver());
    }
}
