package com.beyond.note5.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.beyond.note5.MyApplication;
import com.beyond.note5.R;

public class SplashActivity extends Activity {

    private Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        if(!showSplash()){
            startMainActivity();
            return;
        }
        resetShowSplash();
//        Transition transition = TransitionInflater.from(this).inflateTransition(android.R.transition.fade);
//        getWindow().setExitTransition(transition);

        setContentView(R.layout.activity_splash);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.white));

        initView();


    }

    private void resetShowSplash() {
        MyApplication.getInstance().resetApplicationState();
    }

    private boolean showSplash() {
        return MyApplication.getInstance().isApplicationToBeBorn();
//        return PreferenceUtil.getBoolean("SHOW_SPLASH",true);
    }


    private void initView() {
        ImageView icon = findViewById(R.id.app_icon);
        icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_ic_note5_test,null));

        RotateAnimation animation = new RotateAnimation(0,0,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        animation.setDuration(500);
        animation.setInterpolator(new LinearInterpolator());
        animation.setAnimationListener(new Animation.AnimationListener() {
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
        icon.setAnimation(animation);

        animation.start();
    }

    private void startMainActivity(){
        Intent intent = new Intent(this,MainActivity.class);
//        startActivity(intent,ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        startActivity(intent);
        finish();

//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                finish();
//            }
//        },1000);

    }
}
