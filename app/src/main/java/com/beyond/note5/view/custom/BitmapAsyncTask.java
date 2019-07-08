package com.beyond.note5.view.custom;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

public abstract class BitmapAsyncTask extends AsyncTask<Object,Void,Bitmap> {

    private WeakReference<ImageView> imageViewWeakReference;

    public BitmapAsyncTask(ImageView imageView) {
        this.imageViewWeakReference = new WeakReference<>(imageView);
    }

    @Override
    protected Bitmap doInBackground(Object... params) {
        return getSampledBitmap(params);
    }

    protected abstract Bitmap getSampledBitmap(Object... params);

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()){
            bitmap = null;
        }
        if (imageViewWeakReference != null && bitmap != null) {
            ImageView imageView = imageViewWeakReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    public abstract String getKey();
}
