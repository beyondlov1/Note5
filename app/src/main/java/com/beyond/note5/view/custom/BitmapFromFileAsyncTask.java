package com.beyond.note5.view.custom;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.beyond.note5.utils.BitmapUtil;
import com.beyond.note5.utils.PhotoUtil;

public class BitmapFromFileAsyncTask extends BitmapAsyncTask {

    private String filePath;

    public BitmapFromFileAsyncTask(ImageView imageView) {
        super(imageView);
    }

    @Override
    protected Bitmap getSampledBitmap(Object... bitmapInfos) {
        if (bitmapInfos.length == 0){
            return null;
        }
        if (bitmapInfos[0] instanceof BitmapInfo){
            BitmapInfo bitmapInfo = (BitmapInfo) bitmapInfos[0];
            filePath = bitmapInfo.getFilePath();
            waitFilePrepared();
            Integer width = bitmapInfo.getWidth();
            Integer height = bitmapInfo.getHeight();
            return BitmapUtil.decodeSampledBitmapFromFile(filePath,width,height);
        }else {
            throw new IllegalArgumentException("只能传入BitmapInfo类型");
        }
    }

    private void waitFilePrepared() {
        if (PhotoUtil.isCompressed(filePath)){
            return;
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        waitFilePrepared();
    }

    @Override
    public String getKey() {
        return filePath;
    }
}
