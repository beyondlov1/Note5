package com.beyond.note5.utils;

import android.content.Context;
import android.content.pm.PackageManager;

public class ApkVersionUtil {
    public static int getAppVersionCode(Context context){
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
