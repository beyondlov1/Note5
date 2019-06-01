package com.beyond.note5.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.beyond.note5.R;
import com.beyond.note5.utils.PhotoUtil;

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
        takePhoto();
    }
    private void takePhoto() {
       PhotoUtil.takePhoto(this, TAKE_PHOTO_REQUEST_CODE);
    }

    private static final int TAKE_PHOTO_REQUEST_CODE = 1;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == TAKE_PHOTO_REQUEST_CODE) {
            Log.d("test","hahah");
        }else if (resultCode == RESULT_CANCELED &&requestCode == TAKE_PHOTO_REQUEST_CODE){
            boolean delete = PhotoUtil.getLastPhotoFile().delete();
            Log.d(this.getClass().getSimpleName(),""+delete);
        }

    }
}
