package com.beyond.note5.view.custom;


import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.AttributeSet;

import com.beyond.note5.R;
import com.beyond.note5.view.markdown.decorate.DefaultMarkdownDecorator;
import com.beyond.note5.view.markdown.decorate.MarkdownDecorator;
import com.beyond.note5.view.markdown.decorate.resolver.init.H1LineResolver;
import com.beyond.note5.view.markdown.decorate.resolver.init.H2LineResolver;
import com.beyond.note5.view.markdown.decorate.resolver.init.H3LineResolver;
import com.beyond.note5.view.markdown.decorate.resolver.init.OlLineResolver;
import com.beyond.note5.view.markdown.decorate.resolver.init.UlLineResolver;
import com.beyond.note5.view.markdown.render.DefaultMarkdownRender;
import com.beyond.note5.view.markdown.render.MarkdownRender;

/**
 * @author: beyond
 * @date: 2019/7/19
 */

public class MarkdownAutoRenderEditText extends AppCompatEditText {

    private MarkdownDecorator markdownDecorator;

    private MarkdownRender markdownRender;

    public MarkdownAutoRenderEditText(Context context) {
        this(context, null);
    }

    public MarkdownAutoRenderEditText(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.editTextStyle);
    }

    public MarkdownAutoRenderEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.addTextChangedListener(new MyOnTextChangeListener());
        markdownDecorator = DefaultMarkdownDecorator.createDefault(this);
        markdownRender = createMarkdownRender();
    }

    private MarkdownRender createMarkdownRender() {
        MarkdownRender markdownRender = new DefaultMarkdownRender();
        markdownRender.addResolver(new H1LineResolver());
        markdownRender.addResolver(new H2LineResolver());
        markdownRender.addResolver(new H3LineResolver());
        markdownRender.addResolver(new UlLineResolver());
        markdownRender.addResolver(new OlLineResolver());
        return markdownRender;
    }

    public String getRealContent() {
        if (markdownDecorator == null) {
            return super.getText().toString();
        }
        return markdownDecorator.plain(super.getText());
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (markdownRender == null){
            super.setText(text,type);
            return;
        }
        markdownRender.setBaseTextSize((int)this.getTextSize());
        SpannableStringBuilder renderedText = markdownRender.render(text.toString());
        super.setText(renderedText, type);
    }

    public MarkdownDecorator getMarkdownDecorator() {
        return markdownDecorator;
    }

    private class MyOnTextChangeListener implements TextWatcher {

        private boolean delete = false;

        private int lastLength;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            lastLength = s.length();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            delete = before > 0;
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().endsWith("\n")) {
                removeTextChangedListener(this);

                delete = s.length() < lastLength;
                markdownDecorator.decorate(s, delete);

                addTextChangedListener(this);
            }
            setSelection(getSelectionStart());
        }
    }


    class MyEditable extends SpannableStringBuilder {

        private String realContent;

        @Override
        public String toString() {
            return realContent;
        }

        public void setRealContent(String realContent) {
            this.realContent = realContent;
        }
    }
}
