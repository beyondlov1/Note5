package com.beyond.note5.utils;

import android.content.Context;
import android.graphics.Point;
import android.view.View;
import android.view.WindowManager;

import com.beyond.note5.MyApplication;

public class ViewUtil {

    private static Point screenSize;

    static {
        screenSize = new Point();
        WindowManager systemService = (WindowManager) MyApplication.getInstance().getSystemService(Context.WINDOW_SERVICE);
        if (systemService!=null){
            systemService.getDefaultDisplay().getSize(screenSize);
        }
    }

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
        if (view == null){
            return 0;
        }
        return view.getWidth();
    }
    public static int getHeight(View view){
        if (view == null){
            return 0;
        }
        return view.getHeight();
    }

    public static Point getScreenSize() {
        return screenSize;
    }

}
