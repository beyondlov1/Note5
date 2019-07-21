package com.beyond.note5.view.markdown.decorate;

import android.text.Editable;
import android.widget.TextView;

import com.beyond.note5.view.markdown.decorate.bean.RichLine;
import com.beyond.note5.view.markdown.decorate.bean.RichListLine;
import com.beyond.note5.view.markdown.decorate.plain.H1RichLinePlainer;
import com.beyond.note5.view.markdown.decorate.plain.H2RichLinePlainer;
import com.beyond.note5.view.markdown.decorate.plain.H3RichLinePlainer;
import com.beyond.note5.view.markdown.decorate.plain.OlRichLinePlainer;
import com.beyond.note5.view.markdown.decorate.plain.RichLinePlainer;
import com.beyond.note5.view.markdown.decorate.plain.UlRichLinePlainer;
import com.beyond.note5.view.markdown.decorate.resolver.H1RichLineResolver;
import com.beyond.note5.view.markdown.decorate.resolver.H2RichLineResolver;
import com.beyond.note5.view.markdown.decorate.resolver.H3RichLineResolver;
import com.beyond.note5.view.markdown.decorate.resolver.OlRichLineResolver;
import com.beyond.note5.view.markdown.decorate.resolver.RichLineResolver;
import com.beyond.note5.view.markdown.decorate.resolver.UlRichLineResolver;
import com.beyond.note5.view.markdown.decorate.span.H1TextAppearanceSpan;
import com.beyond.note5.view.markdown.decorate.span.H2TextAppearanceSpan;
import com.beyond.note5.view.markdown.decorate.span.H3TextAppearanceSpan;
import com.beyond.note5.view.markdown.decorate.span.OlMarkdownBulletSpan2;
import com.beyond.note5.view.markdown.decorate.span.UlMarkdownBulletSpan2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author: beyond
 * @date: 2019/7/20
 */

public class DefaultMarkdownDecorator implements MarkdownDecorator {


    public static final String H1 = "# ";
    public static final String H2 = "## ";
    public static final String H3= "### ";
    public static final String H4 = "#### ";
    public static final String H5 = "##### ";
    public static final  String H6= "###### ";
    public static final String UL= "- ";
    private static final String OL = "1.";

    private List<RichLineContext> contexts;

    private RichLineSplitter splitter;

    private DefaultMarkdownDecorator(){
        contexts = new ArrayList<>();
        splitter = new DefaultRichLineSplitter();
    }

    public static DefaultMarkdownDecorator createDefault(){
        DefaultMarkdownDecorator defaultMarkdownDecorator = new DefaultMarkdownDecorator();
        defaultMarkdownDecorator.register(H1, H1TextAppearanceSpan.class,new H1RichLineResolver(),new H1RichLinePlainer());
        defaultMarkdownDecorator.register(H2, H2TextAppearanceSpan.class,new H2RichLineResolver(),new H2RichLinePlainer());
        defaultMarkdownDecorator.register(H3, H3TextAppearanceSpan.class,new H3RichLineResolver(),new H3RichLinePlainer());
        defaultMarkdownDecorator.register(UL, UlMarkdownBulletSpan2.class,new UlRichLineResolver(),new UlRichLinePlainer());
        defaultMarkdownDecorator.register(OL, OlMarkdownBulletSpan2.class,new OlRichLineResolver(),new OlRichLinePlainer());
        return defaultMarkdownDecorator;
    }
    public static DefaultMarkdownDecorator createDefault(TextView textView){
        DefaultMarkdownDecorator defaultMarkdownDecorator = createDefault();
        List<RichLineContext> contexts = defaultMarkdownDecorator.getContexts();
        for (RichLineContext context : contexts) {
            RichLineResolver resolver = context.getResolver();
            resolver.setBaseTextSize((int) textView.getTextSize());
        }
        return defaultMarkdownDecorator;
    }

    @Override
    public void decorate(Editable editable) {
        decorate(editable,false);
    }

    @Override
    public void decorate(Editable editable, boolean delete) {
        List<RichLine> lines = splitter.split(editable);
        Collections.reverse(lines);
        if (lines.isEmpty()){
            return;
        }
        RichLine endLine = lines.get(0);
        if (endLine.getLength()==0 && !delete){
            for (RichLineContext context : contexts) {
                if (context.supportResolve(endLine.getPrev())){
                    if (context.getResolver() instanceof UlRichLineResolver){
                        editable.append(context.getTag());
                    }
                    if (context.getResolver() instanceof OlRichLineResolver){
                        if (endLine.getPrev() instanceof RichListLine){
                            int prevListIndex = ((RichListLine) endLine.getPrev()).getListIndex();
                            editable.append(String.valueOf(prevListIndex+2)).append(". ");
                        }
                    }
                    break;
                }else {
                    if (context.getResolver() instanceof OlRichLineResolver){
                        if (OlRichLineResolver.isListLine(endLine.getPrev())){
                            if (endLine.getPrev() instanceof RichListLine){
                                int prevListIndex = ((RichListLine) endLine.getPrev()).getListIndex();
                                editable.append(String.valueOf(prevListIndex+2)).append(". ");
                            }
                        }
                    }
                }
            }
        }
        for (RichLine line : lines) {
            for (RichLineContext context : contexts) {
                if (context.supportResolve(line)){
                    context.resolve(line);
                    break;
                }
            }
        }
    }

    @Override
    public String plain(Editable editable) {
        StringBuilder raw = new StringBuilder();
        List<RichLine> lines = splitter.split(editable);
        int listIndex = 0;
        for (RichLine line : lines) {
            boolean found = false;
            for (RichLineContext context : contexts) {
                if (context.supportPlain(line)){
                    if (OlRichLinePlainer.isListLine(line)){
                        RichListLine richListLine = new RichListLine(line);
                        richListLine.setListIndex(listIndex);
                        raw.append(context.plain(richListLine));
                        listIndex++;
                    }else {
                        raw.append(context.plain(line));
                    }
                    found = true;
                    break;
                }
            }
            if (!found){
                raw.append(line.getContent());
            }
        }
        return raw.toString();
    }

    @Override
    public void register(String tag, Class span,RichLineResolver resolver,RichLinePlainer plainer ){
        contexts.add(new RichLineContext(tag,span,resolver,plainer));
    }

    public Class<?> getSpanClassByTag(String tag){
        for (RichLineContext context : contexts) {
            if (context.getTag().trim().equals(tag.trim())){
                return context.getSpanClass();
            }
        }
        return null;
    }

    public boolean isDecorated(Editable editable,int start, int end){
        if (start>=end){
            return false;
        }
        Object[] spans = editable.getSpans(start, end, Object.class);
        for (Object span : spans) {
            for (RichLineContext context : contexts) {
                if (context.getSpanClass().equals(span.getClass())){
                    return true;
                }
            }
        }
        return false;
    }

    public List<RichLineContext> getContexts() {
        return contexts;
    }
}
