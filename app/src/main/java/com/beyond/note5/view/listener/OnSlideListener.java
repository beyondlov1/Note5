package com.beyond.note5.view.listener;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author: beyond
 * @date: 2019/2/2
 */

@SuppressWarnings("ALL")
public abstract class OnSlideListener implements View.OnTouchListener {

    private float startX;
    private float startY;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                float endX = event.getX();
                float endY = event.getY();
                float moveX = endX - startX;
                float moveY = endY - startY;

                if (Math.abs(moveX)>Math.abs(moveY)){
                    if (moveX>this.getSlideXSensitivity()){
                        onSlideRight();
                    }else if (moveX<-this.getSlideXSensitivity()){
                        onSlideLeft();
                    }
                } else {
                    if (moveY>getSlideYSensitivity()){
                        onSlideDown();
                    }else if (moveY<-getSlideYSensitivity()){
                        onSlideUp();
                    }
                }

                break;
        }
        return false;
    }

    protected abstract void onSlideLeft();

    protected abstract void onSlideRight();

    protected abstract void onSlideUp();

    protected abstract void onSlideDown();

    private Context context;

    private OnSlideListener(){

    }

    public OnSlideListener(Context context){
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

    protected abstract void onDoubleClick(MotionEvent e);

    protected int getSlideXSensitivity(){
        return 200;
    }

    protected int getSlideYSensitivity(){
        return 300;
    }
}
