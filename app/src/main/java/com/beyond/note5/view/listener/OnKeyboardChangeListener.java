package com.beyond.note5.view.listener;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

import com.beyond.note5.utils.ViewUtil;

import org.greenrobot.eventbus.EventBus;

/**
 * @author: beyond
 * @date: 2019/2/4
 */

public class OnKeyboardChangeListener implements ViewTreeObserver.OnGlobalLayoutListener {

    public Activity context;

    private boolean isShow = false;
    private boolean executeHideCallback = true;
    private Runnable hideCallback;

    public OnKeyboardChangeListener(Activity context){
        this.context = context;
        EventBus.getDefault().postSticky(this);
    }

    @Override
    public void onGlobalLayout() {
        Rect rect = new Rect();
        // 获取当前页面窗口的显示范围
        context.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        int screenWidth = ViewUtil.getScreenSize().x;
        int screenHeight = ViewUtil.getScreenSize().y;
        int keyboardHeight = screenHeight - rect.bottom; // 输入法的高度
        boolean preShowing = isKeyBoardActive();
        if (Math.abs(keyboardHeight) > screenHeight / 5) {
            if (!isShow){
                // 超过屏幕五分之一则表示弹出了输入法
                onKeyBoardShow(screenWidth / 2, keyboardHeight);
                isShow = true;
            }
        } else {
            if (isShow){
                //隐藏输入法
                onKeyBoardHide();
                isShow =false;
            }
        }

    }

    protected void onKeyBoardShow(int x, int y) {
    }

    protected void onKeyBoardHide() {

    }

    private boolean isKeyBoardActive(){
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            return inputMethodManager.isActive();
        }else {
            throw new RuntimeException("获取输入法管理器失败");
        }
    }

    public Runnable getHideCallback() {
        return hideCallback;
    }

    public void setHideCallback(Runnable hideCallback) {
        this.hideCallback = hideCallback;
    }

    public boolean isExecuteHideCallback() {
        return executeHideCallback;
    }

    public void setExecuteHideCallback(boolean executeHideCallback) {
        this.executeHideCallback = executeHideCallback;
    }

}
