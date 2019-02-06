package com.beyond.note5;

import android.app.Application;

import com.beyond.note5.dao.DaoMaster;
import com.beyond.note5.dao.DaoSession;

import org.greenrobot.greendao.database.Database;

/**
 * Created by beyond on 2019/1/30.
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
    }

    private void initDaoSession() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "beyond.db");
        Database database = helper.getEncryptedWritableDb("beyond");
//        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "beyond_no_encrypted.db");
//        SQLiteDatabase database = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(database);
        daoSession = daoMaster.newSession();
    }

    DaoSession daoSession;
    public DaoSession getDaoSession() {
        return daoSession;
    }

}
