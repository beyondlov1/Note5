package com.beyond.note5.permission;

import android.app.Application;

public class ApplicationHolder {
    private static Application application;

    public static void set(Application application){
        ApplicationHolder.application = application;
    }
    public static Application get() {
        return application;
    }
}
