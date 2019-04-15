package com.beyond.note5.view;

import android.app.Activity;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.beyond.note5.R;

public class TestActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        View test_container = findViewById(R.id.test_container);
        Drawable drawable = test_container.getBackground();
        if (drawable instanceof Animatable){
            ((Animatable) drawable).start();
        }

    }
}
