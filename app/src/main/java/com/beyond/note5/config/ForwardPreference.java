package com.beyond.note5.config;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;

import com.beyond.note5.R;

/**
 * @author: beyond
 * @date: 2019/7/27
 */

public class ForwardPreference extends DialogPreference {

    private String forwardClassName;

    public ForwardPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context,attrs,defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ForwardPreference);
        forwardClassName = typedArray.getString(R.styleable.ForwardPreference_forwardClassName);
        typedArray.recycle();
    }

    public ForwardPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.preferenceStyle);
    }

    public ForwardPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onClick() {
        try {
            Class forwardClass = Class.forName(forwardClassName);
            Intent intent = new Intent(getContext(), forwardClass);
            getContext().startActivity(intent);
        } catch (ClassNotFoundException e) {
            Log.e(getClass().getSimpleName(), "跳转class错误");
        }
    }
}
