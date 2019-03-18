package com.beyond.note5.view.custom;

import android.content.Context;
import android.util.AttributeSet;

/**
 * @author beyondlov1
 * @date 2019/03/18
 */
public class SelectionListenableEditText extends android.support.v7.widget.AppCompatEditText {
    public SelectionListenableEditText(Context context) {
        this(context,null);
    }

    public SelectionListenableEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public SelectionListenableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private OnSelectionChangeListener onSelectionChangeListener;

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        onSelectionChangeListener.onChanged(this.getText().toString(),selStart,selEnd);
    }

    public void setOnSelectionChanged(OnSelectionChangeListener onSelectionChangeListener){
        this.onSelectionChangeListener = onSelectionChangeListener;
    }

    public interface OnSelectionChangeListener{
        void onChanged(String content,int selStart, int selEnd);
    }
}
