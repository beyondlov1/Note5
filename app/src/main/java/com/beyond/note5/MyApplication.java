package com.beyond.note5;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Handler;

import com.beyond.note5.bean.Note;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.model.PredictModel;
import com.beyond.note5.model.PredictModelImpl;
import com.beyond.note5.model.dao.DaoMaster;
import com.beyond.note5.model.dao.DaoSession;
import com.beyond.note5.predict.TagPredictor;
import com.beyond.note5.predict.TagPredictorImpl;
import com.beyond.note5.predict.bean.TagGraph;
import com.beyond.note5.predict.train.filter.TimeExpressionTrainTagFilter;
import com.beyond.note5.predict.train.filter.UrlTrainTagFilter;
import com.beyond.note5.sync.Synchronizer;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.DavDataSource;
import com.beyond.note5.sync.datasource.impl.DefaultDavDataSource;
import com.beyond.note5.sync.datasource.impl.NoteDavDataSourceWrap;
import com.beyond.note5.sync.datasource.impl.NoteSqlDataSource;
import com.beyond.note5.sync.datasource.impl.NoteSqlDataSourceWrap;
import com.beyond.note5.sync.datasource.impl.TodoSqlDataSource;
import com.beyond.note5.sync.model.impl.DavSharedTraceInfo;
import com.beyond.note5.sync.synchronizer.DavSynchronizer2;
import com.beyond.note5.sync.webdav.DavLock;
import com.beyond.note5.sync.webdav.client.DavClient;
import com.beyond.note5.sync.webdav.client.SardineDavClient;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.OkWebDavUtil;
import com.beyond.note5.utils.PreferenceUtil;
import com.beyond.note5.utils.ToastUtil;

import org.apache.commons.lang3.StringUtils;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.android.ContextHolder;
import org.greenrobot.greendao.database.Database;
import org.sqldroid.DroidDataSource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.beyond.note5.view.LoginActivity.DAV_LOGIN;
import static com.beyond.note5.view.LoginActivity.DAV_LOGIN_PASSWORD;
import static com.beyond.note5.view.LoginActivity.DAV_LOGIN_USERNAME;

/**
 * @author: beyond
 * @date: 2019/1/30
 */

public class MyApplication extends Application {

    //FIXME: Constrain Preference
    public static final String SHARED_PREFERENCES_NAME = "note5_preferences";
    public static final String DEFAULT_PAGE = "default_page";
    public static final String VIRTUAL_USER_ID = "user.virtual.id";
    public static final String SYNC_REMOTE_URL = "sync.remote.rootUrl";
    private static final String NOTE_SYNC_REMOTE_DAV_SERVERS = "note.sync.remote.dav.servers";
    public static final String NOTE_SYNC_REMOTE_ROOT_PATHS = "note.sync.remote.root.paths";
    private static final String TODO_SYNC_REMOTE_DAV_SERVERS = "todo.sync.remote.dav.servers";
    public static final String TODO_SYNC_REMOTE_ROOT_PATHS = "todo.sync.remote.root.paths";

    public static final String LOG_PATH = "LOCK/sync.log";
    public static final String NOTE_LOCK_PATH = "/LOCK/note_lock.lock";
    public static final String NOTE_LST_PATH = "/LOCK/note_last_sync_time.mark";
    public static final String NOTE_LOG_PATH = "/LOCK/note_sync.log";
    public static final String TODO_LOCK_PATH = "/LOCK/todo_lock.lock";
    public static final String TODO_LST_PATH = "/LOCK/todo_last_sync_time.mark";
    public static final String TODO_LOG_PATH = "/LOCK/todo_sync.log";

    private static MyApplication instance;

    private boolean isApplicationToBeBorn = false;

    public Handler handler = new Handler();

    private Synchronizer<Note> noteSynchronizer;
    private Synchronizer<Todo> todoSynchronizer;

    private DaoSession daoSession;
    private ExecutorService executorService;
    private PredictModel predictModel;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isApplicationToBeBorn = true;
        instance = this;
        initPreference();
        initDaoSession();
        initSynchronizer();

        if (PreferenceUtil.getBoolean(DAV_LOGIN, false)) {
            ToastUtil.toast(getApplicationContext(), "开始同步");
            sync(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.toast(getApplicationContext(), "同步成功");
                }
            }, new Runnable() {
                @Override
                public void run() {
                    ToastUtil.toast(getApplicationContext(), "同步失败");
                }
            });
        }

        File fileStorageDir = getFileStorageDir();
        System.out.println(fileStorageDir.getAbsolutePath());
    }

    public void initPreference() {
        String syncRemoteUrl = PreferenceUtil.getString(SYNC_REMOTE_URL);
        if (StringUtils.isBlank(syncRemoteUrl)) {
            PreferenceUtil.put(SYNC_REMOTE_URL, "https://dav.jianguoyun.com/dav/Note5/data/note.dat");
        }

        String noteSyncRemoteRootUrls = PreferenceUtil.getString(NOTE_SYNC_REMOTE_ROOT_PATHS);
        PreferenceUtil.put(NOTE_SYNC_REMOTE_DAV_SERVERS, "http://192.168.1.103:8070/repository/default/nut3/|https://dav.jianguoyun.com/dav/tera3/");
        PreferenceUtil.put(NOTE_SYNC_REMOTE_ROOT_PATHS, "note/splice1|note/splice2");

        String syncRemoteRootUrls = PreferenceUtil.getString(TODO_SYNC_REMOTE_ROOT_PATHS);
        PreferenceUtil.put(TODO_SYNC_REMOTE_DAV_SERVERS, "http://192.168.1.103:8070/repository/default/nut3/|https://dav.jianguoyun.com/dav/tera3/");
        PreferenceUtil.put(TODO_SYNC_REMOTE_ROOT_PATHS, "todo/splice1|todo/splice2");

        String virtualUserId = PreferenceUtil.getString(VIRTUAL_USER_ID);
        if (StringUtils.isBlank(virtualUserId)) {
            PreferenceUtil.put(VIRTUAL_USER_ID, IDUtil.uuid());
        }
    }

    @SuppressWarnings("unchecked")
    private void initSynchronizer() {

//        DavClient davClient = new SardineDavClient("admin","admin");
        DavClient davClient = new SardineDavClient(PreferenceUtil.getString(DAV_LOGIN_USERNAME), PreferenceUtil.getString(DAV_LOGIN_PASSWORD));

        NoteSqlDataSource noteLocalDataSource = new NoteSqlDataSource();

        List<DavDataSource<Note>> noteDataSources = new ArrayList<>();
        List<DavDataSource<Todo>> todoDataSources = new ArrayList<>();
        String[] noteServers = StringUtils.split(PreferenceUtil.getString(NOTE_SYNC_REMOTE_DAV_SERVERS), "|");
        String[] notePaths = StringUtils.split(PreferenceUtil.getString(NOTE_SYNC_REMOTE_ROOT_PATHS), "|");
        for (String server : noteServers) {
            DefaultDavDataSource<Note> noteDavDataSource = new DefaultDavDataSource.Builder<Note>()
                    .clazz(Note.class)
                    .davClient(davClient)
                    .executorService(null) // 防止坚果云503
                    .server(server)
                    .paths(notePaths)
                    .lock(new DavLock(davClient, OkWebDavUtil.concat(server, NOTE_LOCK_PATH)))
                    .sharedSource(new DavSharedTraceInfo(davClient, OkWebDavUtil.concat(server, NOTE_LST_PATH)))
                    .build();

            noteDataSources.add(new NoteDavDataSourceWrap(noteDavDataSource));
        }
        noteSynchronizer = new DavSynchronizer2.Builder<Note>()
                .localDataSource(new NoteSqlDataSourceWrap(noteLocalDataSource, noteDataSources.get(1)))
                .remoteDataSource(noteDataSources.get(1))
                .logPath(NOTE_LOG_PATH)
                .build();

        DataSource<Todo> todoLocalDataSource = new TodoSqlDataSource();
        String[] todoServers = StringUtils.split(PreferenceUtil.getString(TODO_SYNC_REMOTE_DAV_SERVERS), "|");
        String[] todoPaths = StringUtils.split(PreferenceUtil.getString(TODO_SYNC_REMOTE_ROOT_PATHS), "|");
        for (String server : todoServers) {
            DavDataSource<Todo> todoDavDataSource = new DefaultDavDataSource.Builder<Todo>()
                    .clazz(Todo.class)
                    .davClient(davClient)
                    .executorService(null) // 防止坚果云503
                    .server(server)
                    .paths(todoPaths)
                    .lock(new DavLock(davClient, OkWebDavUtil.concat(server, TODO_LOCK_PATH)))
                    .sharedSource(new DavSharedTraceInfo(davClient, OkWebDavUtil.concat(server, TODO_LST_PATH)))
                    .build();

            todoDataSources.add(todoDavDataSource);
        }


        todoSynchronizer = new DavSynchronizer2.Builder<Todo>()
                .localDataSource(todoLocalDataSource)
                .remoteDataSource(todoDataSources.get(1))
                .logPath(TODO_LOG_PATH)
                .build();
    }

    private void initDaoSession() {

        DroidDataSource dataSource = new DroidDataSource(getPackageName(), "databases/beyond_not_safe");
        ContextHolder.setContext(this);
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setInitOnMigrate(true);
        flyway.migrate();

//        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "beyond.db");
//        Database database = helper.getEncryptedWritableDb("beyond");
//        DaoMaster daoMaster = new DaoMaster(database);
//        daoSession = daoMaster.newSession();

        DaoMaster.OpenHelper helper = new DaoMaster.OpenHelper(this, "beyond_not_safe.db") {
            @Override
            public void onCreate(Database db) {
                // do nothing
            }
        };
        SQLiteDatabase writableDatabase = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(writableDatabase);
        daoSession = daoMaster.newSession();

    }

    public ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = Executors.newCachedThreadPool();
        }
        return executorService;
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public PredictModel getPredictModel() {
        if (predictModel == null) {
            File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            assert storageDir != null;
            TagPredictor<String, TagGraph> tagPredictor = new TagPredictorImpl(
                    new File(storageDir.getAbsolutePath() + File.separator + "model.json"), true);
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

    public void resetApplicationState() {
        isApplicationToBeBorn = false;
    }

    public void sync() {
        sync(null);
    }

    public void sync(Runnable success) {
        sync(success, null);
    }

    public void sync(Runnable success, Runnable fail) {
        if (noteSynchronizer == null || todoSynchronizer == null) {
            initSynchronizer();
        }
        getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    noteSynchronizer.sync();
                    todoSynchronizer.sync();
                    if (success != null) {
                        handler.post(success);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (fail != null) {
                        handler.post(fail);
                    }
                }
            }
        });
    }

    public Synchronizer<Note> getNoteSynchronizer() {
        if (noteSynchronizer == null) {
            initSynchronizer();
        }
        return noteSynchronizer;
    }

    public Synchronizer<Todo> getTodoSynchronizer() {
        if (todoSynchronizer == null) {
            initSynchronizer();
        }
        return todoSynchronizer;
    }

    public File getFileStorageDir() {
        return this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }
}
