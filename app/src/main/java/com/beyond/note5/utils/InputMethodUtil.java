package com.beyond.note5.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.beyond.note5.MyApplication;

/**
 * @author: beyond
 * @date: 2019/2/5
 */

public class InputMethodUtil {
    public static void showKeyboard(final View view) {
        //要设定延迟，延迟不可以是0，不然弹不出来
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    view.requestFocus();
                    inputMethodManager.showSoftInput(view, 0);
                }
            }
        }, 300);
    }

    public static void hideKeyboard(final View view){
        //要设定延迟，延迟不可以是0，不然弹不出来
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }, 300);
    }

    private static int dialogHeightWithSoftInputMethod = 0;
    private static final String DIALOG_HEIGHT_WITH_SOFT_INPUT_METHOD = "dialogHeightWithSoftInputMethod";
    public static int getDialogHeightWithSoftInputMethod(){
        if (dialogHeightWithSoftInputMethod == 0) {
            dialogHeightWithSoftInputMethod =
                    MyApplication.getInstance().getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                            .getInt(DIALOG_HEIGHT_WITH_SOFT_INPUT_METHOD, 0);
        }
        return dialogHeightWithSoftInputMethod;
    }

    public static void rememberDialogHeightWithSoftInputMethod(int value){
        SharedPreferences.Editor editor = MyApplication.getInstance().getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(DIALOG_HEIGHT_WITH_SOFT_INPUT_METHOD, value);
        editor.apply();
    }
}
