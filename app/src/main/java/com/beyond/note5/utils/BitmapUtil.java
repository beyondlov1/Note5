package com.beyond.note5.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.IdRes;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ImageView;

import com.beyond.note5.view.custom.AsyncBitmapDrawable;
import com.beyond.note5.view.custom.BitmapAsyncTask;
import com.beyond.note5.view.custom.BitmapFromFileAsyncTask;
import com.beyond.note5.view.custom.BitmapInfo;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BitmapUtil {

    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = getOptions(res, resId);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private static BitmapFactory.Options getOptions(Resources resources, @IdRes int id) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, id, options);
        return options;
    }

    public static BitmapFactory.Options getOptions(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        return options;
    }

    public static Bitmap scale(Bitmap bitmap, double scale) {
        return Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * scale), (int) (bitmap.getHeight() * scale), false);
    }

    public static void asyncBitmap(Resources resources, ImageView imageView,Bitmap placeHolderBitmap, String filePath) {
        BitmapAsyncTask bitmapAsyncTask = new BitmapFromFileAsyncTask(imageView);
        AsyncBitmapDrawable asyncBitmapDrawable = new AsyncBitmapDrawable(
                resources,
                placeHolderBitmap,
                bitmapAsyncTask);
        Drawable drawable = imageView.getDrawable();
        BitmapInfo bitmapInfo = new BitmapInfo(filePath, 100, 100);
        if (drawable == null){
            // 如果为空说明未设置过， 直接开始异步任务就可以
            imageView.setImageDrawable(asyncBitmapDrawable);
            asyncBitmapDrawable.execute(bitmapInfo);
            Log.d("BitmapUtil","异步显示图片: 直接显示");
        }else if (drawable instanceof AsyncBitmapDrawable) {
            AsyncTask<Object, Void, Bitmap> asyncTask = ((AsyncBitmapDrawable) drawable).getAsyncTask();
            if (asyncTask == null){
                // 如果异步任务为空， 也直接开始
                imageView.setImageDrawable(asyncBitmapDrawable);
                asyncBitmapDrawable.execute(bitmapInfo);
                Log.d("BitmapUtil","异步显示图片: 原来无 asynTask , 直接显示");
            }else if (asyncTask instanceof BitmapAsyncTask) {
                // 如果不为空， 要判断加载的是不是同一个资源， 如果是就什么都不做。 如果不是就停止上一个任务， 开始正确的任务
                // 判断是不是同一个资源是根据key是否相同来判断的
                // 这个的目的主要是防止 recyclerView 这种会复用viewholder的控件保留着上次的任务
                String oldKey = ((BitmapAsyncTask) asyncTask).getKey();
                if (!StringUtils.equals(oldKey, filePath)) {
                    asyncTask.cancel(true);
                    imageView.setImageDrawable(asyncBitmapDrawable);
                    asyncBitmapDrawable.execute(bitmapInfo);
                    Log.d("BitmapUtil","异步显示图片: 复用之前的 viewholder");
                }
            }
        }else {
            imageView.setImageDrawable(asyncBitmapDrawable);
            asyncBitmapDrawable.execute(bitmapInfo);
            Log.d("BitmapUtil","异步显示图片: 不是异步显示bitmap类型, 直接显示");
        }
    }

    private static Map<Integer,SparseArray<Bitmap>> placeHolderBitmaps;

    public static Bitmap getPlaceHolderBitmap(int width, int height){
        if (placeHolderBitmaps == null){
            synchronized (BitmapUtil.class){
                if (placeHolderBitmaps == null) {
                    placeHolderBitmaps = new ConcurrentHashMap<>();
                }
            }
        }

        if (placeHolderBitmaps.get(width) == null){
            placeHolderBitmaps.put(width, new SparseArray<>());
        }
        if (placeHolderBitmaps.get(width).get(height) == null){
            synchronized (BitmapUtil.class) {
                if (placeHolderBitmaps.get(width).get(height) == null){
                    placeHolderBitmaps.get(width).put(height, Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888));
                }
            }
        }

        return placeHolderBitmaps.get(width).get(height);
    }

    public static Bitmap getPlaceHolderBitmap() {
        return BitmapHolder.instance;
    }

    public static double getHeightWidthFactor(String path) {
        BitmapFactory.Options options = BitmapUtil.getOptions(path);
        return (double) options.outHeight / (double) options.outWidth;
    }

    private static class BitmapHolder{
        public static final Bitmap instance = Bitmap.createBitmap(new int[]{Color.GRAY},1,1,Bitmap.Config.ARGB_8888);
    }
}
