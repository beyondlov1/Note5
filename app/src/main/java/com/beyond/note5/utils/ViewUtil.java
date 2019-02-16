package com.beyond.note5.utils;

import android.view.View;

public class ViewUtil {

    public static int getXInScreenWithoutNotification(View view){
        int[] xy = new int[2];
        view.getLocationInWindow(xy);
        return xy[0];
    }
    public static int getYInScreenWithoutNotification(View view){
        int[] xy = new int[2];
        view.getLocationInWindow(xy);
        return xy[1]-75;
    }
    public static int getWidth(View view){
        return view.getWidth();
    }
    public static int getHeight(View view){
        return view.getHeight();
    }

}
