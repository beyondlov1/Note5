package com.beyond.note5;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.beyond.note5.model.PredictModel;
import com.beyond.note5.model.PredictModelImpl;
import com.beyond.note5.model.dao.DaoMaster;
import com.beyond.note5.model.dao.DaoSession;
import com.beyond.note5.predict.TagPredictor;
import com.beyond.note5.predict.TagPredictorImpl;
import com.beyond.note5.predict.bean.TagGraph;
import com.beyond.note5.predict.train.filter.TimeExpressionTrainTagFilter;
import com.beyond.note5.predict.train.filter.UrlTrainTagFilter;
import com.beyond.note5.utils.PreferenceUtil;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: beyond
 * @date: 2019/1/30
 */

public class MyApplication extends Application {

    //FIXME: Constrain Preference
    public static final String SHARED_PREFERENCES_NAME = "note5_preferences";
    public static final String DEFAULT_PAGE = "default_page";
    public static final String IS_ALTER_SQL_EXECUTED = "IS_ALTER_SQL_EXECUTED";

    private boolean isApplicationToBeBorn = false;

    private static MyApplication instance;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isApplicationToBeBorn = true;
        instance = this;
        initDaoSession();
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

        //addColumnDev("note","PRIORITY","int");
    }

    //FIXME
    @SuppressWarnings("SameParameterValue")
    private void addColumnDev(String tableName, String column, String columnType){
        if (PreferenceUtil.getBoolean(IS_ALTER_SQL_EXECUTED)){
            return;
        }
        SQLiteOpenHelper sqLiteOpenHelper = new SQLiteOpenHelper(this,"beyond_not_safe.db",null,1) {
            @Override
            public void onCreate(SQLiteDatabase db) {

            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            }
        };
        sqLiteOpenHelper.getWritableDatabase().execSQL("alter table "+tableName+" add column "+column+" "+columnType);
        PreferenceUtil.put(IS_ALTER_SQL_EXECUTED,true);
    }


    private ExecutorService executorService;
    public ExecutorService getExecutorService() {
        if (executorService == null){
            executorService = Executors.newCachedThreadPool();
        }
        return executorService;
    }

    private DaoSession daoSession;

    public DaoSession getDaoSession() {
        return daoSession;
    }

    private PredictModel predictModel;

    public PredictModel getPredictModel() {
        if (predictModel == null){
            File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            assert storageDir != null;
            TagPredictor<String,TagGraph> tagPredictor = new TagPredictorImpl(
                    new File(storageDir.getAbsolutePath()+File.separator+"model.json"),true);
            tagPredictor.addTrainFilter(new UrlTrainTagFilter());
            tagPredictor.addTrainFilter(new TimeExpressionTrainTagFilter());
            tagPredictor.setExecutorService(executorService);
            predictModel = PredictModelImpl.getRelativeSingletonInstance(tagPredictor);
        }
        return predictModel;
    }

    public boolean isApplicationToBeBorn() {
        return isApplicationToBeBorn;
    }

    public void resetApplicationState(){
        isApplicationToBeBorn = false;
    }
}
