package com.beyond.note5.utils;

import android.content.Context;
import android.widget.Toast;

import com.beyond.note5.MyApplication;

public class ToastUtil {
    private static Toast toast;
    public static void toast(Context context,String msg,int duration){
        if (toast == null){
            toast = Toast.makeText(context, msg, duration);
            toast.show();
        }else {
            toast.setDuration(duration);
            toast.setText(msg);
            toast.show();
        }
    }
    public static void toast(Context context,String msg){
        toast(context, msg, Toast.LENGTH_SHORT);
    }

    public static void toast(String msg){
        toast(MyApplication.getInstance(), msg, Toast.LENGTH_SHORT);
    }

    public static void cancel(){
        if (toast!=null){
            toast.cancel();
        }
    }
}
