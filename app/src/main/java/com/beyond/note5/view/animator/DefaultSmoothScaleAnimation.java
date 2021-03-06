package com.beyond.note5.view.animator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.beyond.note5.utils.ViewUtil;

public class DefaultSmoothScaleAnimation implements SmoothScaleAnimation {

    protected View container;
    protected View startView;
    protected View endView;
    protected View showingView;

    protected Runnable afterShowHook;
    protected Runnable afterHideHook;
    protected Runnable beforeShowHook;
    protected Runnable beforeHideHook;

    private AnimatorSet showAnimatorSet = new AnimatorSet();
    private AnimatorSet hideAnimatorSet = new AnimatorSet();
    private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();

    private boolean showAnimatorRunning = false;
    private boolean hideAnimatorRunning = false;

    @Override
    public View getContainer() {
        return container;
    }

    @Override
    public void setContainer(View container) {
        this.container = container;
    }

    @Override
    public View getStartView() {
        return startView;
    }

    @Override
    public void setStartView(View startView) {
        this.startView = startView;
    }

    @Override
    public View getEndView() {
        return endView;
    }

    @Override
    public void setEndView(View endView) {
        this.endView = endView;
    }

    @Override
    public View getShowingView() {
        return showingView;
    }

    @Override
    public void setShowingView(View showingView) {
        this.showingView = showingView;
    }

    @Override
    public void show() {

        if (showAnimatorRunning){
            return;
        }

        showAnimatorRunning = true;

        if (beforeShowHook!=null){
            beforeShowHook.run();
        }

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
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1f).setDuration(300);
        showAnimatorSet.setInterpolator(decelerateInterpolator);
        showAnimatorSet.playTogether(valueAnimator);
        showAnimatorSet.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                container.setX(clickItemX - animatedValue * clickItemX);
                container.setY(clickItemY - animatedValue * clickItemY);
                container.getLayoutParams().width = (int) (clickItemWidth + animatedValue * (containerWidth - clickItemWidth));
                container.getLayoutParams().height = (int) (clickItemHeight + animatedValue * (containerHeight - clickItemHeight));
                container.setLayoutParams(container.getLayoutParams());
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (afterShowHook != null) {
                    afterShowHook.run();
                }
                showAnimatorRunning = false;
            }
        });
    }

    @Override
    public void hide() {

        if (hideAnimatorRunning){
            return;
        }

        hideAnimatorRunning = true;

        if (beforeHideHook!=null){
            beforeHideHook.run();
        }

        //获取view 位置、大小信息
        final int clickItemWidth = ViewUtil.getWidth(endView);
        final int clickItemHeight = ViewUtil.getHeight(endView);
        final float clickItemX = ViewUtil.getXInScreenWithoutNotification(endView);
        final float clickItemY = ViewUtil.getYInScreenWithoutNotification(endView);
        final int containerWidth = ViewUtil.getWidth(showingView);
        final int containerHeight = ViewUtil.getHeight(showingView);

        //出现动画
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1f, 0).setDuration(300);
        hideAnimatorSet.playTogether(valueAnimator);
        hideAnimatorSet.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                if (container == null) {
                    return;
                }
                container.setX(clickItemX - animatedValue * clickItemX);
                container.setY(clickItemY - animatedValue * clickItemY);
                container.getLayoutParams().width = (int) (clickItemWidth + animatedValue * (containerWidth - clickItemWidth));
                container.getLayoutParams().height = (int) (clickItemHeight + animatedValue * (containerHeight - clickItemHeight));
                container.setLayoutParams(container.getLayoutParams());
            }
        });

        hideAnimatorSet.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (afterHideHook != null) {
                    afterHideHook.run();
                }
                hideAnimatorRunning =false;
            }
        });
    }

    @Override
    public void setBeforeShowHook(Runnable runnable) {
        this.beforeShowHook = runnable;
    }

    @Override
    public void setBeforeHideHook(Runnable runnable) {
        this.beforeHideHook = runnable;
    }


    @Override
    public void setAfterShowHook(Runnable onShownListener) {
        this.afterShowHook = onShownListener;
    }

    @Override
    public void setAfterHideHook(Runnable onHiddenListener) {
        this.afterHideHook = onHiddenListener;

    }

}
