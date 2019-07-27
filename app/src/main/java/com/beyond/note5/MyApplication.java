package com.beyond.note5;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.beyond.note5.bean.Account;
import com.beyond.note5.bean.Note;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.model.AccountModel;
import com.beyond.note5.model.AccountModelImpl;
import com.beyond.note5.model.NoteModel;
import com.beyond.note5.model.NoteModelImpl;
import com.beyond.note5.model.PredictModel;
import com.beyond.note5.model.PredictModelImpl;
import com.beyond.note5.model.dao.DaoMaster;
import com.beyond.note5.model.dao.DaoSession;
import com.beyond.note5.predict.Predictor;
import com.beyond.note5.predict.PredictorImpl;
import com.beyond.note5.predict.bean.TagGraph;
import com.beyond.note5.predict.filter.train.TimeExpressionTrainFilter;
import com.beyond.note5.predict.filter.train.UrlTrainFilter;
import com.beyond.note5.predict.filter.train.UselessTrainFilter;
import com.beyond.note5.service.schedule.ScheduleReceiver;
import com.beyond.note5.service.schedule.callback.SyncScheduleCallback;
import com.beyond.note5.service.schedule.utils.ScheduleUtil;
import com.beyond.note5.sync.Synchronizer;
import com.beyond.note5.sync.datasource.DavDataSource;
import com.beyond.note5.sync.datasource.impl.DefaultDavDataSource;
import com.beyond.note5.sync.datasource.impl.NoteDavDataSourceWrap;
import com.beyond.note5.sync.datasource.impl.NoteSqlDataSource;
import com.beyond.note5.sync.datasource.impl.NoteSqlDataSourceWrap;
import com.beyond.note5.sync.datasource.impl.TodoSqlDataSource;
import com.beyond.note5.sync.datasource.impl.TodoSqlDataSourceWrap;
import com.beyond.note5.sync.model.impl.DavSharedTraceInfo;
import com.beyond.note5.sync.synchronizer.DavSynchronizer4;
import com.beyond.note5.sync.webdav.DavLock;
import com.beyond.note5.sync.webdav.client.DavClient;
import com.beyond.note5.sync.webdav.client.SardineDavClient;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.OkWebDavUtil;
import com.beyond.note5.utils.PreferenceUtil;
import com.beyond.note5.utils.ToastUtil;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.android.ContextHolder;
import org.greenrobot.greendao.database.Database;
import org.sqldroid.DroidDataSource;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: beyond
 * @date: 2019/1/30
 */

public class MyApplication extends Application {

    // Constant
    public static final String SHARED_PREFERENCES_NAME = "note5_preferences";
    public static final String LOCK_DIR = "/LOCK";
    public static final String LOG_PATH = "LOCK/sync.log";
    public static final String NOTE_LOCK_PATH = "/LOCK/note_lock.lock";
    public static final String LOGIN_PATH = "/LOCK/";
    public static final String NOTE_LST_PATH = "/LOCK/note_last_sync_time.mark";
    public static final String NOTE_LOG_PATH = "/LOCK/note_sync.log";
    public static final String TODO_LOCK_PATH = "/LOCK/todo_lock.lock";
    public static final String TODO_LST_PATH = "/LOCK/todo_last_sync_time.mark";
    public static final String TODO_LOG_PATH = "/LOCK/todo_sync.log";
    public static final String DAV_ROOT_DIR = "test/version2";

    // Preference Name Auto Config
    public static final String DEFAULT_PAGE = "default_page";
    public static final String NOTE_SYNC_REMOTE_ROOT_PATHS = "note.sync.remote.root.paths";
    public static final String TODO_SYNC_REMOTE_ROOT_PATHS = "todo.sync.remote.root.paths";

    // Preference Name Manual Configurable
    public static final String VIRTUAL_USER_ID = "user.virtual.id";
    public static final String SYNC_SHOULD_SCHEDULE = "sync.schedule.enabled";
    public static final String NOTE_NOTIFICATION_SHOULD_SCHEDULE = "note.notification.schedule.enabled";
    public static final String NOTE_SHOULD_EDIT_MARKDOWN_JUST_IN_TIME = "todo.markdown.edit.render.jit.enabled";
    public static final String TODO_SHOULD_TRAIN = "todo.train.enabled";
    public static final String FLOAT_BUTTON_SHOULD_SHOW = "float.button.enabled";

    private static MyApplication instance;

    private boolean isApplicationToBeBorn = false;

    public Handler handler = new Handler();

    private List<Synchronizer<Note>> noteSynchronizers;
    private List<Synchronizer<Todo>> todoSynchronizers;

    private DaoSession daoSession;
    private ExecutorService executorService;
    private NoteModel noteModel;
    private PredictModel predictModel;
    private AccountModel accountModel;

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
        startNotificationScanner();
        scheduleSyncService();

//        syncWithToast();

    }

    private void scheduleSyncService() {
        boolean shouldSchedule = PreferenceUtil.getBoolean(SYNC_SHOULD_SCHEDULE, false);

        String userId = PreferenceUtil.getString(VIRTUAL_USER_ID);
        int syncTimeOffset = Math.abs(userId.hashCode()) % 60; // 防止多个设备同时同步冲突
        if (shouldSchedule) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(1992, Calendar.SEPTEMBER, 25, 0, 0, 0);
            calendar.add(Calendar.MINUTE,syncTimeOffset);
            ScheduleReceiver.cancel(this,ScheduleReceiver.SYNC_REQUEST_CODE);
            ScheduleReceiver.scheduleRepeat(this,ScheduleReceiver.SYNC_REQUEST_CODE,
                    calendar.getTimeInMillis(),24*60*60*1000,SyncScheduleCallback.class);
        } else {
            ScheduleReceiver.cancel(this,ScheduleReceiver.SYNC_REQUEST_CODE);
        }
    }

    private void startNotificationScanner() {
        boolean shouldSchedule = PreferenceUtil.getBoolean(NOTE_NOTIFICATION_SHOULD_SCHEDULE, false);
        if (!shouldSchedule) {
            return;
        }
        List<Note> toNotifyNote = getNoteModel().findByPriority(5);
        for (Note note : toNotifyNote) {
            try {
                if (!ScheduleUtil.isSet(note)){
                    Date lastModifyTime = note.getLastModifyTime();
                    if (lastModifyTime == null){
                        continue;
                    }
                    ScheduleUtil.scheduleNotificationFrom(note, lastModifyTime.getTime());
                }
            }catch (Exception e){
                Log.e(getClass().getSimpleName(),"初始化定时任务设置失败:"+note.getId(),e);
            }
        }
    }

    public void initPreference() {
        PreferenceUtil.put(NOTE_SYNC_REMOTE_ROOT_PATHS, "note/splice1|note/splice2");
        PreferenceUtil.put(TODO_SYNC_REMOTE_ROOT_PATHS, "todo/splice1|todo/splice2");

        String virtualUserId = PreferenceUtil.getString(VIRTUAL_USER_ID);
        if (StringUtils.isBlank(virtualUserId)) {
            PreferenceUtil.put(VIRTUAL_USER_ID, IDUtil.uuid());
        }
    }

    @SuppressWarnings("unchecked")
    private void initSynchronizer() {

        accountModel = new AccountModelImpl();
        List<Account> accounts = accountModel.findAllValid();

        if (accounts.isEmpty()) {
            return;
        }

        noteSynchronizers = new ArrayList<>();
        todoSynchronizers = new ArrayList<>();

        for (Account account : accounts) {
            String server = account.getServer();
            DavClient davClient = new SardineDavClient(account.getUsername(), account.getPassword());

            NoteSqlDataSource noteLocalDataSource = new NoteSqlDataSource();
            String[] notePaths = StringUtils.split(PreferenceUtil.getString(NOTE_SYNC_REMOTE_ROOT_PATHS), "|");

            String server1 = OkWebDavUtil.concat(server, DAV_ROOT_DIR);
            DefaultDavDataSource<Note> noteDavDataSource1 = new DefaultDavDataSource.Builder<Note>()
                    .clazz(Note.class)
                    .davClient(davClient)
                    .executorService(null) // 防止坚果云503
                    .server(server1)
                    .paths(notePaths)
                    .lock(new DavLock(davClient, OkWebDavUtil.concat(server1, NOTE_LOCK_PATH)))
                    .sharedSource(new DavSharedTraceInfo(davClient, OkWebDavUtil.concat(server1, NOTE_LST_PATH)))
                    .build();


            NoteDavDataSourceWrap noteDavDataSourceWrap = new NoteDavDataSourceWrap(noteDavDataSource1);
            noteSynchronizers.add(new DavSynchronizer4.Builder<Note>()
                    .localDataSource(new NoteSqlDataSourceWrap(noteLocalDataSource, noteDavDataSourceWrap))
                    .remoteDataSource(noteDavDataSourceWrap)
                    .logPath(NOTE_LOG_PATH)
                    .build());

            TodoSqlDataSource todoLocalDataSource = new TodoSqlDataSource();
            String[] todoPaths = StringUtils.split(PreferenceUtil.getString(TODO_SYNC_REMOTE_ROOT_PATHS), "|");
            DavDataSource<Todo> todoDavDataSource1 = new DefaultDavDataSource.Builder<Todo>()
                    .clazz(Todo.class)
                    .davClient(davClient)
                    .executorService(null) // 防止坚果云503
                    .server(server1)
                    .paths(todoPaths)
                    .lock(new DavLock(davClient, OkWebDavUtil.concat(server1, TODO_LOCK_PATH)))
                    .sharedSource(new DavSharedTraceInfo(davClient, OkWebDavUtil.concat(server1, TODO_LST_PATH)))
                    .build();

            todoSynchronizers.add(new DavSynchronizer4.Builder<Todo>()
                    .localDataSource(new TodoSqlDataSourceWrap(todoLocalDataSource, todoDavDataSource1))
                    .remoteDataSource(todoDavDataSource1)
                    .logPath(TODO_LOG_PATH)
                    .build());
        }

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

//        daoSession.getAccountDao().deleteAll();
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

    public boolean isApplicationToBeBorn() {
        return isApplicationToBeBorn;
    }

    public void resetApplicationState() {
        isApplicationToBeBorn = false;
    }

    /** sync start */

    public void sync() {
        sync(null);
    }

    public void sync(Runnable success) {
        sync(success, null);
    }

    public void sync(Runnable success, Runnable fail) {
        if (CollectionUtils.isEmpty(noteSynchronizers) || CollectionUtils.isEmpty(todoSynchronizers)) {
            initSynchronizer();
        }
        getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                boolean isSuccess = true;
                for (Synchronizer<Note> synchronizer : noteSynchronizers) {
                    try {
                        synchronizer.sync();
                    } catch (Exception e) {
                        e.printStackTrace();
                        isSuccess = false;
                    }
                }
                for (Synchronizer<Todo> synchronizer : todoSynchronizers) {
                    try {
                        synchronizer.sync();
                    } catch (Exception e) {
                        e.printStackTrace();
                        isSuccess = false;
                    }
                }

                if (isSuccess) {
                    if (success != null) {
                        handler.post(success);
                    }
                } else {
                    if (fail != null) {
                        handler.post(fail);
                    }
                }
            }
        });
    }

    public void syncAllWithToast() {
        if (CollectionUtils.isNotEmpty(noteSynchronizers) && CollectionUtils.isNotEmpty(todoSynchronizers)) {
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
    }

    public void refreshSynchronizers() {
        initSynchronizer();
    }

    public List<Synchronizer<Note>> getNoteSynchronizers() {
        if (CollectionUtils.isEmpty(noteSynchronizers)) {
            initSynchronizer();
        }
        return noteSynchronizers;
    }

    public List<Synchronizer<Todo>> getTodoSynchronizers() {
        if (CollectionUtils.isEmpty(todoSynchronizers)) {
            initSynchronizer();
        }
        return todoSynchronizers;
    }

    /** sync end */

    public File getFileStorageDir() {
        return this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

    public PredictModel getPredictModel() {
        if (predictModel == null) {
            File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            assert storageDir != null;
            Predictor<String, TagGraph> predictor = new PredictorImpl(
                    new File(storageDir.getAbsolutePath() + File.separator + "model.json"), true);
            predictor.addTrainFilter(new UselessTrainFilter());
            predictor.addTrainFilter(new UrlTrainFilter());
            predictor.addTrainFilter(new TimeExpressionTrainFilter());
            predictor.setExecutorService(executorService);
            predictModel = PredictModelImpl.getRelativeSingletonInstance(predictor);
        }
        return predictModel;
    }

    public AccountModel getAccountModel() {
        return accountModel;
    }

    public NoteModel getNoteModel(){
        if (noteModel == null){
            noteModel = NoteModelImpl.getSingletonInstance();
        }
        return noteModel;
    }
}
