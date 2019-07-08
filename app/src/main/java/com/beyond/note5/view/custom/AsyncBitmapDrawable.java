package com.beyond.note5.view.custom;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

public class AsyncBitmapDrawable extends BitmapDrawable {
    private WeakReference<AsyncTask<Object,Void,Bitmap>> asyncTaskWeakReference;

    public AsyncBitmapDrawable(Resources res, Bitmap bitmap, AsyncTask<Object,Void,Bitmap> asyncTask) {
        super(res, bitmap);
        this.asyncTaskWeakReference = new WeakReference<>(asyncTask);
    }

    public AsyncTask<Object, Void, Bitmap> getAsyncTask() {
        return asyncTaskWeakReference.get();
    }

    public void execute(Object...params){
        AsyncTask<Object, Void, Bitmap> asyncTask = asyncTaskWeakReference.get();
        if (asyncTask!=null){
            asyncTask.execute(params);
        }
    }
}
