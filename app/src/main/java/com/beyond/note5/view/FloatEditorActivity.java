package com.beyond.note5.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.beyond.note5.R;
import com.beyond.note5.event.AfterFloatEditorSavedEvent;
import com.beyond.note5.utils.StatusBarUtil;
import com.beyond.note5.view.fragment.TodoEditFragment;
import com.beyond.note5.view.listener.OnKeyboardChangeListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class FloatEditorActivity extends FragmentActivity implements View.OnClickListener {

    View activityContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_float_editor);
        initStatusBar();
        initFragment();
        initView();
        initEvent();
    }

    private void initStatusBar(){
        StatusBarUtil.showTransparentStatusBar(this);
    }

    private void initView() {
        activityContainer = findViewById(R.id.float_editor_activity_container);
    }

    private void initEvent() {
        activityContainer.setOnClickListener(this);
        //监控输入法
        OnKeyboardChangeListener onKeyboardChangeListener = new MyOnKeyboardChangeListener(this);
        this.getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(onKeyboardChangeListener);
    }

    private void initFragment() {
        Fragment fragment = new TodoEditFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.float_editor_fragment_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onClick(View v) {
        if (activityContainer == v){
            finish();
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(AfterFloatEditorSavedEvent event){
        finish();
    }

    @Override
    public void onBackPressed() {
        EventBus.getDefault().post(new AfterFloatEditorSavedEvent(null));
//        super.onBackPressed();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().post(new AfterFloatEditorSavedEvent(null));
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private class MyOnKeyboardChangeListener extends OnKeyboardChangeListener {

        MyOnKeyboardChangeListener(Activity context) {
            super(context);
        }

        @Override
        protected void onKeyBoardShow(int x, int y) {
        }

        @Override
        protected void onKeyBoardHide() {
            finish();
        }
    }

}
