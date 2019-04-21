package com.beyond.note5.view.animator.svg;

import android.content.Context;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.RequiresApi;
import android.view.View;

public class VectorAnimationImpl implements VectorAnimation {
    protected Context context;
    protected View target;
    private AnimatedVectorDrawable animatedVectorDrawable;

    public VectorAnimationImpl(Context context) {
        this.context = context;
    }

    @Override
    public void setVectorDrawable(@DrawableRes Integer id) {
        animatedVectorDrawable = (AnimatedVectorDrawable) context.getResources().getDrawable(id, null);
    }

    @Override
    public void setTarget(View target) {
        this.target = target;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void registerAnimationCallback(Animatable2.AnimationCallback callback) {
        animatedVectorDrawable.registerAnimationCallback(callback);
    }

    @Override
    public void start() {
        if (target == null) {
            throw new RuntimeException("target is necessary");
        }

        if (animatedVectorDrawable == null) {
            return;
        }

        target.setBackground(animatedVectorDrawable);
        animatedVectorDrawable.start();
    }
}
