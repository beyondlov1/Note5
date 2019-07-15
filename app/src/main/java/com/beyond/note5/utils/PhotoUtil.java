package com.beyond.note5.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;

import com.github.dfqin.grantor.PermissionListener;
import com.github.dfqin.grantor.PermissionsUtil;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.FileCallback;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class PhotoUtil {

    private static File photoFile;

    public static void takePhoto(Activity activity, int requestCode) {

        //获取缩略图
//        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//用来打开相机的Intent
//        if(takePhotoIntent.resolveActivity(getPackageManager())!=null){//这句作用是如果没有相机则该应用不会闪退，要是不加这句则当系统没有相机应用的时候该应用会闪退
//            startActivityForResult(takePhotoIntent,1);//启动相机
//        }

        if (!PermissionsUtil.hasPermission(activity, Manifest.permission.CAMERA)){
            PermissionsUtil.requestPermission(activity, new PermissionListener() {
                @Override
                public void permissionGranted(@NonNull String[] permission) {
                    doTakePhoto(activity, requestCode);
                }

                @Override
                public void permissionDenied(@NonNull String[] permission) {
                    ToastUtil.toast(activity,"获取相机权限失败");
                }
            }, new String[]{Manifest.permission.CAMERA}, true, null);
        }else {
            doTakePhoto(activity,requestCode);
        }
    }

    private static void doTakePhoto(Activity activity, int requestCode) {
        //获取完整图
        Uri mImageUri;
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//打开相机的Intent
        if (takePhotoIntent.resolveActivity(activity.getPackageManager()) != null) {//这句作用是如果没有相机则该应用不会闪退，要是不加这句则当系统没有相机应用的时候该应用会闪退
            File imageFile = createImageFile(activity);//创建用来保存照片的文件
            if (imageFile != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    /*7.0以上要通过FileProvider将File转化为Uri*/
                    mImageUri = FileProvider.getUriForFile(activity, "com.beyond.note5", imageFile);
                } else {
                    /*7.0以下则直接使用Uri的fromFile方法将File转化为Uri*/
                    mImageUri = Uri.fromFile(imageFile);
                }
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);//将用于输出的文件Uri传递给相机
                activity.startActivityForResult(takePhotoIntent, requestCode);//打开相机
            }
            photoFile = imageFile;
            return;
        }
        photoFile = null;
    }

    public static File getLastPhotoFile(){
        return photoFile;
    }

    public static File createImageFile(Context context) {
        return createImageFile(context,"jpg");
    }

    public static File createImageFile(Context context,String suffix) {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = null;
        try {
            imageFile = File.createTempFile(imageFileName, "."+suffix, storageDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageFile;
    }

//    private void galleryAddPic() {
//        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        File f = new File(absolutePath);
//        Uri contentUri = Uri.fromFile(f);
//        mediaScanIntent.setData(contentUri);
//        this.sendBroadcast(mediaScanIntent);
//    }

    private static List<String> compressingFilePaths = Collections.synchronizedList(new ArrayList<>());
    public static void compressImage(String path){
        compressingFilePaths.add(path);
        compressImage(path, new FileCallback() {
            @Override
            public void callback(boolean isSuccess, String outfile) {
                compressingFilePaths.remove(path);
            }
        });
    }

    public static boolean isCompressed(String path){
        return !compressingFilePaths.contains(path);
    }

    public static void compressImage(String path,FileCallback callback){
        Tiny.FileCompressOptions options = new Tiny.FileCompressOptions();
        options.overrideSource = true;
        Tiny.getInstance()
                .source(path)
                .asFile()
                .withOptions(options)
                .compress(callback);
    }
}
