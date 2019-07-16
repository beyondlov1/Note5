package com.beyond.note5.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.beyond.note5.R;

/**
 * @author: beyond
 * @date: 2019/2/6
 */

public class AutoSizeTextView extends android.support.v7.widget.AppCompatTextView {

    private float mScaleFactor;

    private Float baseTextSize;

    public AutoSizeTextView(Context context) {
        this(context, null);
    }

    public AutoSizeTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public AutoSizeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AutoSizeTextView);
        mScaleFactor = typedArray.getFloat(R.styleable.AutoSizeTextView_scaleFactor, 0.618f);
        typedArray.recycle();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (text == null) {
            return;
        }
        computeAndSetTextSize(text);
        super.setText(text, type);
    }

    public void computeAndSetTextSize(CharSequence text) {
        if (baseTextSize == null){
            baseTextSize = getTextSize();
        }
        float targetSize = (float) (baseTextSize * (1 + mScaleFactor * Math.pow(1.618, -text.length() / 10)));
        setTextSize(TypedValue.COMPLEX_UNIT_PX, targetSize);
    }

}
