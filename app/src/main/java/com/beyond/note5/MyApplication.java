package com.beyond.note5;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import com.beyond.note5.model.dao.DaoMaster;
import com.beyond.note5.model.dao.DaoSession;
import com.beyond.note5.predict.TagPredictor;

import java.io.File;

/**
 * @author: beyond
 * @date: 2019/1/30
 */

public class MyApplication extends Application {

    public static final String SHARED_PREFERENCES_NAME = "note5_preferences";
    public static final String DEFAULT_PAGE = "default_page";


    private static MyApplication instance;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initDaoSession();
        initTagPredict();
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

    private void initTagPredict() {
        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        assert storageDir != null;
        tagPredictor = new TagPredictor(new File(storageDir.getAbsolutePath()+File.separator+"model.json"));
    }

    private TagPredictor tagPredictor;

    public TagPredictor getTagPredictor() {
        return tagPredictor;
    }

    private DaoSession daoSession;

    public DaoSession getDaoSession() {
        return daoSession;
    }

}
