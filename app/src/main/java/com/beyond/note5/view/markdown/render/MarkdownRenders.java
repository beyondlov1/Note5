package com.beyond.note5.view.markdown.render;

import android.text.SpannableStringBuilder;
import android.widget.TextView;

import com.beyond.note5.view.custom.AutoSizeTextView;

/**
 * @author: beyond
 * @date: 2019/7/16
 */

public abstract class MarkdownRenders {
    private static  MarkdownRender markdownRender = MarkdownRenderHolder.INSTANCE;
    private static class MarkdownRenderHolder{
        static final MarkdownRender INSTANCE =  new DefaultMarkdownRender();
    }
    public static MarkdownRender getDefaultRender(){
        return markdownRender;
    }
    public static void render(TextView textView, String source){
        if (textView instanceof AutoSizeTextView){
            ((AutoSizeTextView) textView).computeAndSetTextSize(source);
        }
        markdownRender.setBaseTextSize((int)textView.getTextSize());
        SpannableStringBuilder renderedText = markdownRender.render(source);
        textView.setText(renderedText);
    }
}
