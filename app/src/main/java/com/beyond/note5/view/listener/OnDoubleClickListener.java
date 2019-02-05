package com.beyond.note5.view.listener;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by beyond on 2019/2/2.
 */

public abstract class OnDoubleClickListener implements View.OnTouchListener {

    private Context context;

    public OnDoubleClickListener(Context context){
        this.context = context;
    }

    private GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

        /**
         * 双击发生时的通知
         * @param e
         * @return
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {//双击事件
            onDoubleClick(e);
            return super.onDoubleTap(e);
        }

        /**
         * 双击手势过程中发生的事件，包括按下、移动和抬起事件
         * @param e
         * @return
         */
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

    protected abstract void onDoubleClick(MotionEvent e);
}
