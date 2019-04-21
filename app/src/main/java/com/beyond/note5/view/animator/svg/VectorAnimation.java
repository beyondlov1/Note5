package com.beyond.note5.view.animator.svg;

import android.graphics.drawable.Animatable2;
import android.support.annotation.DrawableRes;
import android.view.View;

public interface VectorAnimation {
    void start();
    void registerAnimationCallback(Animatable2.AnimationCallback callback);
    void setVectorDrawable(@DrawableRes Integer id);
    void setTarget(View target);
}
