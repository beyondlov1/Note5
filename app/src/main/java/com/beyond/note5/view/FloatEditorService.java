package com.beyond.note5.view;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.beyond.note5.R;
import com.beyond.note5.utils.ToastUtil;

import static android.view.WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;

public class FloatEditorService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startFloat();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startFloat() {
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        View viewGroup = LayoutInflater.from(this).inflate(R.layout.fragment_note_edit, null);

        // 新建悬浮窗控件
        Button button = new Button(getApplicationContext());
        button.setText("Floating Window");
        button.setBackgroundColor(Color.BLUE);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.width = 500;
        layoutParams.height = 800;
        layoutParams.x = 0;
        layoutParams.y = -300;
        layoutParams.flags = FLAG_NOT_FOCUSABLE|FLAG_WATCH_OUTSIDE_TOUCH|FLAG_ALT_FOCUSABLE_IM;

        // 将悬浮窗控件添加到WindowManager
//        windowManager.addView(button, layoutParams);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(),FloatEditorActivity.class));
                ToastUtil.toast(getApplicationContext(),"hello");
            }
        });
        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                button.setVisibility(View.GONE);
                return true;
            }
        });
        windowManager.addView(button,layoutParams);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
