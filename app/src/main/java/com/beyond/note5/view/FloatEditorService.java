package com.beyond.note5.view;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.beyond.note5.R;
import com.beyond.note5.event.AfterFloatEditorSavedEvent;
import com.beyond.note5.event.HideFloatButtonEvent;
import com.beyond.note5.event.ShowFloatButtonEvent;
import com.beyond.note5.utils.ViewUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

import static android.view.WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;

public class FloatEditorService extends Service {


    private boolean visibilityFlag = true;

    ImageButton button;

    WindowManager windowManager;

    WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);

        init();

    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        button = new ImageButton(getApplicationContext());
        // 新建悬浮窗控件

        button.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher_round, null));
        button.setBackgroundColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.width = button.getDrawable().getMinimumWidth();
        layoutParams.height = button.getDrawable().getMinimumWidth();
        layoutParams.x = ViewUtil.getScreenSize().x / 2 - 50;
        layoutParams.y = -ViewUtil.getScreenSize().y / 2 + 50;
        layoutParams.flags = FLAG_NOT_FOCUSABLE | FLAG_WATCH_OUTSIDE_TOUCH | FLAG_ALT_FOCUSABLE_IM;

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), FloatEditorActivity.class));
                button.setVisibility(View.GONE);
            }
        });
        button.setOnTouchListener(new OnTouchMovingListener().longClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                button.setVisibility(View.GONE);
                return false;
            }
        }));
        windowManager.addView(button, layoutParams);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        visibilityFlag = Objects.requireNonNull(intent.getExtras()).getBoolean("showFloatButton");
        startFloat();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startFloat() {
        if (!visibilityFlag) {
            button.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void show(AfterFloatEditorSavedEvent event) {
        visibilityFlag = true;
        button.setVisibility(View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void show(ShowFloatButtonEvent event) {
        visibilityFlag = true;
        button.setVisibility(View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void hide(HideFloatButtonEvent event) {
        visibilityFlag = false;
        button.setVisibility(View.GONE);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class OnTouchMovingListener implements View.OnTouchListener {

        private int x;
        private int y;

        private int startX;
        private int startY;

        private long startTimeMills;

        private View.OnLongClickListener longClickListener;

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    startX = x;
                    startY = y;
                    startTimeMills = System.currentTimeMillis();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;
                    windowManager.updateViewLayout(v, layoutParams);

                    if (System.currentTimeMillis() - startTimeMills > 2000){
                        int totalMovedX = nowX - startX;
                        int totalMovedY = nowY - startY;
                        if (Math.abs(totalMovedX) + Math.abs(totalMovedY) < 20) {
                            button.setVisibility(View.GONE);
                            return true;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    int endX = (int) event.getRawX();
                    int endY = (int) event.getRawY();
                    int totalMovedX = endX - startX;
                    int totalMovedY = endY - startY;

                    if (System.currentTimeMillis() - startTimeMills > 2000){
                        if (Math.abs(totalMovedX) + Math.abs(totalMovedY) < 20) {
                            longClickListener.onLongClick(v);
                            return true;
                        }
                    }else {
                        if (Math.abs(totalMovedX) + Math.abs(totalMovedY) < 20) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                default:
                    break;
            }
            return false;
        }

        public View.OnLongClickListener getLongClickListener() {
            return longClickListener;
        }

        public void setLongClickListener(View.OnLongClickListener longClickListener) {
            this.longClickListener = longClickListener;
        }

        public OnTouchMovingListener longClickListener(View.OnLongClickListener longClickListener) {
            this.longClickListener = longClickListener;
            return this;
        }
    }
}
