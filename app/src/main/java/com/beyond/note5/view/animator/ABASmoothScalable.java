package com.beyond.note5.view.animator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;

import com.beyond.note5.utils.ViewUtil;

public class ABASmoothScalable extends DefaultSmoothScalable {
    @Override
    public void show() {

        //获取view 位置、大小信息
        final int clickItemWidth = ViewUtil.getWidth(startView);
        final int clickItemHeight = ViewUtil.getHeight(startView);
        final float clickItemX = ViewUtil.getXInScreenWithoutNotification(startView);
        final float clickItemY = ViewUtil.getYInScreenWithoutNotification(startView);

        final int containerWidth = ViewUtil.getWidth(showingView);
        final int containerHeight = ViewUtil.getHeight(showingView);
        //设置初始位置
        container.getLayoutParams().width = clickItemWidth;
        container.getLayoutParams().height = clickItemHeight;
        container.setX(clickItemX);
        container.setY(clickItemY);

        //出现动画
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1.7f, 1f).setDuration(600);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.playTogether(valueAnimator);
        animatorSet.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();

                if (animation.getCurrentPlayTime()<300){
                    container.setX(clickItemX - animatedValue * clickItemX);
                    container.setY((float) (clickItemY - clickItemY/2.4*animatedValue));
                    container.getLayoutParams().width = (int) (clickItemWidth + animatedValue * (containerWidth - clickItemWidth));
                    container.getLayoutParams().height = (int) (clickItemHeight + animatedValue * (containerHeight - clickItemHeight));
                    container.setLayoutParams(container.getLayoutParams());
                }else{
                    container.setX(clickItemX - animatedValue * clickItemX);
                    container.setY((float) (0.417*animatedValue*clickItemY-0.417*clickItemY));
                    container.getLayoutParams().width = (int) (clickItemWidth + animatedValue * (containerWidth - clickItemWidth));
                    container.getLayoutParams().height = (int) (clickItemHeight + animatedValue * (containerHeight - clickItemHeight));
                    container.setLayoutParams(container.getLayoutParams());
                }

            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (onShownListener!=null){
                    onShownListener.run();
                }
            }
        });
    }
}
