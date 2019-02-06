package com.beyond.note5;

import android.app.Application;

import com.beyond.note5.model.dao.DaoMaster;
import com.beyond.note5.model.dao.DaoSession;

import org.greenrobot.greendao.database.Database;

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
    }

    private void initDaoSession() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "beyond.db");
        Database database = helper.getEncryptedWritableDb("beyond");
        DaoMaster daoMaster = new DaoMaster(database);
        daoSession = daoMaster.newSession();
    }

    DaoSession daoSession;
    public DaoSession getDaoSession() {
        return daoSession;
    }

}
