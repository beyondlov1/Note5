package com.beyond.note5.utils;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by beyond on 2019/2/5.
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
        }, 100);
    }
}
