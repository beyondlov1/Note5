package com.beyond.note5.utils;

import android.content.Context;
import android.os.Environment;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

public class FileUtil {

    public static File savePicture(Context context, String fileName){
        fileName = StringUtils.removeEnd(fileName, ".jpg");
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = null;
        try {
            imageFile = File.createTempFile(fileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageFile;
    }
}
