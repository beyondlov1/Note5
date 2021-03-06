package com.beyond.note5.view.animator;

import android.view.View;

public interface SmoothScaleAnimation {
    void setContainer(View view);
    View getContainer();
    void setStartView(View view);
    View getStartView();
    void setEndView(View view);
    View getEndView();
    void setShowingView(View view);
    View getShowingView();
    void show();
    void hide();
    void setBeforeShowHook(Runnable runnable);
    void setBeforeHideHook(Runnable runnable);
    void setAfterShowHook(Runnable runnable);
    void setAfterHideHook(Runnable runnable);
}
