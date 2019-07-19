package com.beyond.note5.view.custom;


import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.AttributeSet;

import com.beyond.note5.R;
import com.beyond.note5.view.markdown.decorate.DefaultRichLineSplitter;
import com.beyond.note5.view.markdown.decorate.RichLine;
import com.beyond.note5.view.markdown.decorate.RichLineResolver;
import com.beyond.note5.view.markdown.decorate.RichLineSplitter;
import com.beyond.note5.view.markdown.decorate.RichListLine;
import com.beyond.note5.view.markdown.decorate.resolver.H1RichLineResolver;
import com.beyond.note5.view.markdown.decorate.resolver.H2RichLineResolver;
import com.beyond.note5.view.markdown.decorate.resolver.H3RichLineResolver;
import com.beyond.note5.view.markdown.decorate.resolver.OlRichLineResolver;
import com.beyond.note5.view.markdown.decorate.resolver.UlRichLineResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author: beyond
 * @date: 2019/7/19
 */

public class MarkdownAutoRenderEditText extends AppCompatEditText {


    private RichLineSplitter splitter;

    private List<RichLineResolver> resolvers;

    public MarkdownAutoRenderEditText(Context context) {
        this(context, null);
    }

    public MarkdownAutoRenderEditText(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.editTextStyle);
    }

    public MarkdownAutoRenderEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TextWatcher textChangeListener = new MyOnTextChangeListener();
        this.addTextChangedListener(textChangeListener);
        splitter = new DefaultRichLineSplitter();
        resolvers = new ArrayList<>();
        resolvers.add(new H1RichLineResolver());
        resolvers.add(new H2RichLineResolver());
        resolvers.add(new H3RichLineResolver());
        resolvers.add(new UlRichLineResolver());
        resolvers.add(new OlRichLineResolver());
    }

    public String getRealContent(){
        if (splitter == null){
            return super.getText().toString();
        }
        StringBuilder raw = new StringBuilder();
        List<RichLine> lines = splitter.split(super.getText());
        int listIndex = 0;
        for (RichLine line : lines) {
            boolean found = false;
            for (RichLineResolver resolver : resolvers) {
                if (resolver.supportPlain(line)){
                    if (OlRichLineResolver.isListLine(line)){
                        RichListLine richListLine = new RichListLine(line);
                        richListLine.setListIndex(listIndex);
                        raw.append(resolver.plain(richListLine));
                        listIndex++;
                    }else {
                        raw.append(resolver.plain(line));
                    }
                    found = true;
                }
            }
            if (!found){
                raw.append(line.getContent());
            }
        }
        return raw.toString();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        MyEditable myEditable = new MyEditable();
        myEditable.append(text);
        super.setText(myEditable, type);
    }

    private class MyOnTextChangeListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().endsWith("\n")) {
                removeTextChangedListener(this);

                List<RichLine> lines = splitter.split(s);
                Collections.reverse(lines);
                for (RichLine line : lines) {
                    for (RichLineResolver resolver : resolvers) {
                        if (resolver.supportResolve(line)) {
                            resolver.resolve(line);
                        }
                    }
                }

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
