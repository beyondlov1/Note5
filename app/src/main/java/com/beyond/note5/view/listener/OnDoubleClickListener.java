package com.beyond.note5.view.listener;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author: beyond
 * @date: 2019/2/2
 */

public abstract class OnDoubleClickListener implements View.OnTouchListener {

    private Context context;

    public OnDoubleClickListener(Context context){
        this.context = context;
    }

    private GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDoubleTap(MotionEvent e) {//双击事件
            onDoubleClick(e);
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return super.onDoubleTapEvent(e);
        }
    });

    @Override
    public boolean onTouch(View v, MotionEvent event) {
         gestureDetector.onTouchEvent(event);
         return false;
    }

    abstract void onDoubleClick(MotionEvent e);
}
