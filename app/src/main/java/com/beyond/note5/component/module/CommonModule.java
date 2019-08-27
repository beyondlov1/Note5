package com.beyond.note5.component.module;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Handler;

import com.beyond.note5.MyApplication;
import com.beyond.note5.model.AccountModel;
import com.beyond.note5.model.AccountModelImpl;
import com.beyond.note5.model.NoteModel;
import com.beyond.note5.model.NoteModelImpl;
import com.beyond.note5.model.PredictModel;
import com.beyond.note5.model.PredictModelImpl;
import com.beyond.note5.model.TodoModel;
import com.beyond.note5.model.TodoModelImpl;
import com.beyond.note5.model.dao.DaoMaster;
import com.beyond.note5.model.dao.DaoSession;
import com.beyond.note5.predict.Predictor;
import com.beyond.note5.predict.PredictorImpl;
import com.beyond.note5.predict.bean.TagGraph;
import com.beyond.note5.predict.filter.train.TimeExpressionTrainFilter;
import com.beyond.note5.predict.filter.train.UrlTrainFilter;
import com.beyond.note5.predict.filter.train.UselessTrainFilter;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.android.ContextHolder;
import org.greenrobot.greendao.database.Database;
import org.sqldroid.DroidDataSource;

import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

/**
 * @author: beyond
 * @date: 2019/8/27
 */
@Module
public class CommonModule {

    private static Handler handler;
    private static ThreadPoolExecutor threadPoolExecutor;
    private static OkHttpClient okHttpClient;
    private static DaoSession daoSession;
    private static PredictModel predictModel;
    private static AccountModel accountModel;
    private static NoteModel noteModel;
    private static TodoModel todoModel;

    @Singleton
    @Provides
    Handler provideHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }

    @Singleton
    @Provides
    ThreadPoolExecutor provideThreadPoolExecutor() {
        if (threadPoolExecutor == null) {
            threadPoolExecutor = new ThreadPoolExecutor(
                    0, 60,
                    60, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>());
        }
        return threadPoolExecutor;
    }

    @Singleton
    @Provides
    OkHttpClient provideOkHttpClient() {
        if (okHttpClient == null) {
            OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
            httpBuilder.connectTimeout(10000, TimeUnit.MILLISECONDS);
            httpBuilder.readTimeout(10000, TimeUnit.MILLISECONDS);
            okHttpClient = httpBuilder.build();
        }
        return okHttpClient;
    }

    @Singleton
    @Provides
    DaoSession provideDaoSession() {
        if (daoSession == null) {
            final MyApplication application = MyApplication.getInstance();
            DroidDataSource dataSource = new DroidDataSource(application.getPackageName(), "databases/beyond_not_safe");
            ContextHolder.setContext(application);
            Flyway flyway = new Flyway();
            flyway.setDataSource(dataSource);
            flyway.setInitOnMigrate(true);
            flyway.migrate();

//        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "beyond.db");
//        Database database = helper.getEncryptedWritableDb("beyond");
//        DaoMaster daoMaster = new DaoMaster(database);
//        daoSession = daoMaster.newSession();

            DaoMaster.OpenHelper helper = new DaoMaster.OpenHelper(application, "beyond_not_safe.db") {
                @Override
                public void onCreate(Database db) {
                    // do nothing
                }
            };
            SQLiteDatabase writableDatabase = helper.getWritableDatabase();
            DaoMaster daoMaster = new DaoMaster(writableDatabase);
            daoSession = daoMaster.newSession();
        }
        return daoSession;
    }

    @Singleton
    @Provides
    @Inject
    PredictModel providePredictModel(ThreadPoolExecutor threadPoolExecutor) {
        final MyApplication application = MyApplication.getInstance();
        if (predictModel == null) {
            File storageDir = application.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            assert storageDir != null;
            Predictor<String, TagGraph> predictor = new PredictorImpl(
                    new File(storageDir.getAbsolutePath() + File.separator + "model.json"), true);
            predictor.addTrainFilter(new UselessTrainFilter());
            predictor.addTrainFilter(new UrlTrainFilter());
            predictor.addTrainFilter(new TimeExpressionTrainFilter());
            predictor.setExecutorService(threadPoolExecutor);
            predictModel = PredictModelImpl.getRelativeSingletonInstance(predictor);
        }
        return predictModel;
    }

    @Singleton
    @Provides
    AccountModel provideAccountModel() {
        if (accountModel == null) {
            accountModel = new AccountModelImpl();
        }
        return accountModel;
    }

    @Singleton
    @Provides
    NoteModel provideNoteModel() {
        if (noteModel == null) {
            noteModel = new NoteModelImpl();
        }
        return noteModel;
    }

    @Singleton
    @Provides
    TodoModel provideTodoModel() {
        if (todoModel == null) {
            todoModel = new TodoModelImpl();
        }
        return todoModel;
    }
}
