package com.beyond.note5.view.custom;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;

/**
 * @author: beyond
 * @date: 2019/2/6
 */

public class AutoSizeTextView extends android.support.v7.widget.AppCompatTextView {

    public AutoSizeTextView(Context context) {
        super(context);
    }

    public AutoSizeTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoSizeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (getTextSize()*(1+ 0.618f*Math.pow(1.618, - text.length() / 10))));
        super.setText(text, type);
    }
}
