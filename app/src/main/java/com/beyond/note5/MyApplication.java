package com.beyond.note5;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.beyond.note5.model.dao.DaoMaster;
import com.beyond.note5.model.dao.DaoSession;

import java.io.File;

/**
 * @author: beyond
 * @date: 2019/1/30
 */

public class MyApplication extends Application {

    public static final String  SHARED_PREFERENCES_NAME = "note5_preferences";

    private static MyApplication instance;
    public static MyApplication getInstance(){
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initDaoSession();

        File file = this.getFileStreamPath("pic.jpg");
        Log.d("MyApplication", Uri.fromFile(file).toString());

    }

    private void initDaoSession() {
//        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "beyond.db");
//        Database database = helper.getEncryptedWritableDb("beyond");
//        DaoMaster daoMaster = new DaoMaster(database);
//        daoSession = daoMaster.newSession();

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "beyond_not_safe.db");
        SQLiteDatabase writableDatabase = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(writableDatabase);
        daoSession = daoMaster.newSession();
    }

    DaoSession daoSession;
    public DaoSession getDaoSession() {
        return daoSession;
    }

}
