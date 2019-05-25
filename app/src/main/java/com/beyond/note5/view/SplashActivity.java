package com.beyond.note5.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.beyond.note5.MyApplication;
import com.beyond.note5.R;

public class SplashActivity extends Activity {

    private boolean splash;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
//        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
//        Transition transition = TransitionInflater.from(this).inflateTransition(android.R.transition.fade);
//        getWindow().setEnterTransition(transition);
//        getWindow().setExitTransition(transition);

        if (!shouldSplash()) {
            splash = false;
            startMainActivity();
            return;
        }

        splash = true;

        resetSplashState();

        showSplashView();

    }

    private void showSplashView() {
        setContentView(R.layout.activity_splash);
        initStatusBar();
        initView();
    }

    private void initStatusBar() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
    }

    private void resetSplashState() {
        MyApplication.getInstance().resetApplicationState();
    }

    private boolean shouldSplash() {
        return MyApplication.getInstance().isApplicationToBeBorn();
    }


    private void initView() {
        ImageView icon = findViewById(R.id.app_icon);
        icon.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher_round, null));
//        RotateAnimation animation = new RotateAnimation(0,0,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
//        animation.setDuration(500);
//        animation.setInterpolator(new LinearInterpolator());
//        animation.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                startMainActivity();
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//        icon.setAnimation(animation);
//
//        animation.start();

        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
        alphaAnimation.setDuration(618);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startMainActivity();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        icon.setAnimation(alphaAnimation);
        alphaAnimation.start();
    }

    private void startMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
//        startActivity(intent,ActivityOptions.makeSceneTransitionAnimation(SplashActivity.this).toBundle());
        startActivity(intent);
        if(splash){
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        }
        finish();
    }
}
