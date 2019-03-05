package com.beyond.note5.utils;

import android.content.Context;

import com.beyond.note5.MyApplication;

public class PreferenceUtil {
    public static void put(String key, String value){
        MyApplication.getInstance().getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME,Context.MODE_PRIVATE)
                .edit()
                .putString(key, value)
                .apply();
    }
    public static void put(String key, int value){
        MyApplication.getInstance().getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME,Context.MODE_PRIVATE)
                .edit()
                .putInt(key, value)
                .apply();
    }

    public static boolean getBoolean(String key){
       return MyApplication.getInstance().getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME,Context.MODE_PRIVATE)
                .getBoolean(key,false);
    }

    public static String getString(String key){
       return MyApplication.getInstance().getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME,Context.MODE_PRIVATE)
                .getString(key,null);
    }

    public static int getInt(String key) {
        return MyApplication.getInstance().getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME,Context.MODE_PRIVATE)
                .getInt(key,0);
    }
}
