package com.beyond.note5.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.beyond.note5.MyApplication;

import java.util.HashMap;
import java.util.Map;

public class ViewUtil {

    private static Point screenSize;
    private static Point screenSizeWithoutNotification;


    static {
        screenSize = new Point();
        WindowManager systemService = (WindowManager) MyApplication.getInstance().getSystemService(Context.WINDOW_SERVICE);
        if (systemService != null) {
            systemService.getDefaultDisplay().getSize(screenSize);
        }

        screenSizeWithoutNotification = new Point();
        screenSizeWithoutNotification.x = screenSize.x;
        screenSizeWithoutNotification.y = screenSize.y - 70;
    }

    private static int getX(View view) {
        if (view == null) {
            return 0;
        }
        return (int) view.getX();
    }

    private static int getY(View view) {
        if (view == null) {
            return 0;
        }
        return (int) view.getY();
    }

    public static int getXInScreenWithoutNotification(View view) {
        if (view == null){
            return 0;
        }
        int[] xy = new int[2];
        view.getLocationInWindow(xy);
        return xy[0] == 0 ? getX(view) : xy[0];
    }

    public static int getYInScreenWithoutNotification(View view) {
        if (view == null){
            return 0;
        }
        int[] xy = new int[2];
        view.getLocationInWindow(xy);
        return (xy[1] == 0 ? getY(view) : xy[1]) - 75;
    }

    public static int getWidth(View view) {
        if (view == null) {
            return 0;
        }
        return view.getWidth();
    }

    public static int getHeight(View view) {
        if (view == null) {
            return 0;
        }
        return view.getHeight();
    }

    public static Point getScreenSize() {
        return screenSize;
    }

    public static Point getScreenSizeWithoutNotification() {
        return screenSizeWithoutNotification;
    }


    private static Map<Object, Map<Object, View>> rightBottomViews = new HashMap<>(1);
    private static Map<Object, Map<Object, View>> leftTopViews = new HashMap<>(1);

    public static View getRightBottomView(Activity activity, View tpl) {
        if (rightBottomViews.containsKey(activity)) {
            if (rightBottomViews.get(activity).containsKey(tpl)) {
                return rightBottomViews.get(activity).get(tpl);
            }
        }
        View rightBottomView = new View(activity);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(tpl.getLayoutParams());
        layoutParams.width = 0;
        layoutParams.height = 0;
        rightBottomView.setLayoutParams(layoutParams);
        rightBottomView.setX(ViewUtil.getScreenSizeWithoutNotification().x);
        rightBottomView.setY(ViewUtil.getScreenSizeWithoutNotification().y);
        HashMap<Object, View> hashMap = new HashMap<>(1);
        hashMap.put(tpl, rightBottomView);
        rightBottomViews.put(activity, hashMap);
        return rightBottomView;
    }

    public static View getLeftTopView(Activity activity, View tpl) {

        if (leftTopViews.containsKey(activity)) {
            if (leftTopViews.get(activity).containsKey(tpl)) {
                return leftTopViews.get(activity).get(tpl);
            }
        }
        View leftTopView = new View(activity);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(tpl.getLayoutParams());
        layoutParams.width = 0;
        layoutParams.height = 0;
        leftTopView.setLayoutParams(layoutParams);
        leftTopView.setX(ViewUtil.getScreenSizeWithoutNotification().x);
        leftTopView.setY(ViewUtil.getScreenSizeWithoutNotification().y);
        HashMap<Object, View> hashMap = new HashMap<>(1);
        hashMap.put(tpl, leftTopView);
        leftTopViews.put(activity, hashMap);
        return leftTopView;

    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     */
    public static float getSpFromPx(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return pxValue / fontScale + 0.5f;
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     */
    public static float getPxFromSp(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return spValue * fontScale + 0.5f;
    }
}
