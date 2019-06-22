package com.beyond.note5;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Handler;

import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Note;
import com.beyond.note5.model.PredictModel;
import com.beyond.note5.model.PredictModelImpl;
import com.beyond.note5.model.dao.DaoMaster;
import com.beyond.note5.model.dao.DaoSession;
import com.beyond.note5.predict.TagPredictor;
import com.beyond.note5.predict.TagPredictorImpl;
import com.beyond.note5.predict.bean.TagGraph;
import com.beyond.note5.predict.train.filter.TimeExpressionTrainTagFilter;
import com.beyond.note5.predict.train.filter.UrlTrainTagFilter;
import com.beyond.note5.sync.model.impl.LSTDavModel;
import com.beyond.note5.sync.Synchronizer;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.DavDataSource;
import com.beyond.note5.sync.datasource.DistributedDavDataSource;
import com.beyond.note5.sync.datasource.note.NoteLocalDataSource;
import com.beyond.note5.sync.synchronizer2.DavSynchronizer2;
import com.beyond.note5.sync.webdav.DavLock;
import com.beyond.note5.sync.webdav.client.DavClient;
import com.beyond.note5.sync.webdav.client.SardineDavClient;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.OkWebDavUtil;
import com.beyond.note5.utils.PreferenceUtil;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.android.ContextHolder;
import org.greenrobot.greendao.database.Database;
import org.sqldroid.DroidDataSource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    public static final String SYNC_REMOTE_URL = "syncNote.remote.rootUrl";
    public static final String SYNC_REMOTE_ROOT_PATHS = "syncNote.remote.root.paths";
    private static final String SYNC_REMOTE_DAV_SERVERS = "syncNote.remote.dav.servers";

    public static final String LOG_PATH = "LOCK/sync.log";
    public static final String LOCK_PATH = "/LOCK/distributeLock.lock";
    public static final String LST_PATH = "/LOCK/lastSyncTime.mark";


    private boolean isApplicationToBeBorn = false;

    private Handler handler = new Handler();

    private static MyApplication instance;

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
            syncNote();
        }
    }

    @SuppressWarnings("unchecked")
    private void initSynchronizer() {
        DataSource<Note> localDataSource = new NoteLocalDataSource();

        DavClient davClient = new SardineDavClient(PreferenceUtil.getString(DAV_LOGIN_USERNAME), PreferenceUtil.getString(DAV_LOGIN_PASSWORD));
        List<DavDataSource<Note>> dataSources = new ArrayList<>();
        String[] servers = StringUtils.split(PreferenceUtil.getString(SYNC_REMOTE_DAV_SERVERS), "|");
        String[] paths = StringUtils.split(PreferenceUtil.getString(SYNC_REMOTE_ROOT_PATHS), "|");
        for (String server : servers) {
            DavDataSource davDataSource = new DistributedDavDataSource.Builder<Note>()
                    .clazz(Note.class)
                    .davClient(davClient)
                    .executorService(getExecutorService())
                    .server(server)
                    .paths(paths)
                    .lock(new DavLock(davClient, OkWebDavUtil.concat(server, LOCK_PATH)))
                    .lstRecorder(new LSTDavModel(davClient, OkWebDavUtil.concat(server, LST_PATH)))
                    .build();

            dataSources.add(davDataSource);
        }

        synchronizer = new DavSynchronizer2.Builder<Note>()
                .localDataSource(localDataSource)
                .remoteDataSource(dataSources.get(0))
                .build();
    }

    public void initPreference() {
        String syncRemoteUrl = PreferenceUtil.getString(SYNC_REMOTE_URL);
        if (StringUtils.isBlank(syncRemoteUrl)) {
            PreferenceUtil.put(SYNC_REMOTE_URL, "https://dav.jianguoyun.com/dav/Note5/data/note.dat");
        }

        String syncRemoteRootUrls = PreferenceUtil.getString(SYNC_REMOTE_ROOT_PATHS);
        PreferenceUtil.put(SYNC_REMOTE_DAV_SERVERS, "https://dav.jianguoyun.com/dav/nut3/|https://dav.jianguoyun.com/dav/tera3/");
        PreferenceUtil.put(SYNC_REMOTE_ROOT_PATHS, "note/splice1|note/splice2");

        if (StringUtils.isBlank(syncRemoteRootUrls)) {
        }

        String virtualUserId = PreferenceUtil.getString(VIRTUAL_USER_ID);
        if (StringUtils.isBlank(virtualUserId)) {
            PreferenceUtil.put(VIRTUAL_USER_ID, IDUtil.uuid());
        }
    }

    private Date setTime(int year, int month, int day) {
        Calendar instance = Calendar.getInstance();
        instance.set(year, month, day);
        return instance.getTime();
    }

    private void initDaoSession() {

        DroidDataSource dataSource = new DroidDataSource(getPackageName(), "databases/beyond_not_safe_2");
        ContextHolder.setContext(this);
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setInitOnMigrate(true);
        flyway.migrate();

//        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "beyond.db");
//        Database database = helper.getEncryptedWritableDb("beyond");
//        DaoMaster daoMaster = new DaoMaster(database);
//        daoSession = daoMaster.newSession();

        DaoMaster.OpenHelper helper = new DaoMaster.OpenHelper(this, "beyond_not_safe_2.db"){
            @Override
            public void onCreate(Database db) {
                // do nothing
            }
        };
        SQLiteDatabase writableDatabase = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(writableDatabase);
        daoSession = daoMaster.newSession();

    }

    private ExecutorService executorService;

    public ExecutorService getExecutorService() {
        if (executorService == null) {
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

    private Synchronizer<Note> synchronizer;

    public void syncNote() {
        syncNote(null);
    }

    public void syncNote(Runnable success) {
        syncNote(success, null);
    }

    public void syncNote(Runnable success, Runnable fail) {
        if (synchronizer == null) {
            initSynchronizer();
        }
        getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronizer.sync();
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


    public void test(DataSource<Note> localDataSource, DataSource<Note> remoteDataSource, Synchronizer<Note> synchronizer) throws IOException {
        Note note = Note.newInstance();
        Note note1 = Note.newInstance();
        Note note2 = Note.newInstance();
        Note note3 = Note.newInstance();
        Note note4 = Note.newInstance();
        Note note5 = Note.newInstance();
        Note note6 = Note.newInstance();

        note.setId("0");
        note1.setId("1");
        note2.setId("2");
        note3.setId("3");
        note4.setId("4");
        note5.setId("5");
        note6.setId("6");

        note.setLastModifyTime(setTime(2019, 1, 12));
        note1.setLastModifyTime(setTime(2018, 1, 12));
        note2.setLastModifyTime(setTime(2019, 1, 1));
        note3.setLastModifyTime(setTime(2019, 1, 12));
        note4.setLastModifyTime(setTime(2017, 1, 12));
        note5.setLastModifyTime(setTime(2020, 1, 12));
        note6.setLastModifyTime(setTime(2020, 1, 12));

        note.setTitle("note");
        note1.setTitle("note1");
        note2.setTitle("note2");
        note3.setTitle("note3");
        note4.setTitle("note4");
        note5.setTitle("note5");
        note6.setTitle("note6");

        List<Attachment> attachments = new ArrayList<>();
        note.setAttachments(attachments);
        note1.setAttachments(attachments);
        note2.setAttachments(attachments);
        note3.setAttachments(attachments);
        note4.setAttachments(attachments);
        note5.setAttachments(attachments);
        note6.setAttachments(attachments);


        List<Note> localList = new ArrayList<>();
        localList.add(note);  // add
        localList.add(note1);
//        localList.add(note2); //delete

        Note note3Clone = ObjectUtils.clone(note3);
        note3Clone.setLastModifyTime(setTime(2021, 10, 4));
        note3Clone.setVersion(note3.getVersion() + 1);
        localList.add(note3Clone); //update
        localList.add(note4);
        localList.add(note6);

        List<Note> remoteList = new ArrayList<>();
        remoteList.add(note1);
        remoteList.add(note2);
        Note note3CloneR = ObjectUtils.clone(note3);
        note3CloneR.setLastModifyTime(setTime(2021, 10, 4));
        note3CloneR.setVersion(note3.getVersion() + 2);
        remoteList.add(note3CloneR); //remote update sameone
//        remoteList.add(note4);  // remote delete
        remoteList.add(note5); // remote add
        Note note6Clone = ObjectUtils.clone(note6);
        note6Clone.setLastModifyTime(setTime(2021, 10, 4));
        note6Clone.setVersion(note6.getVersion() + 1);
        remoteList.add(note6Clone); // remote update

        try {
            synchronizer.sync();
            System.out.println();
            System.out.println();

            for (Note n : localDataSource.selectAll()) {
                System.out.println(n.getTitle());
            }

            for (Note n : remoteDataSource.selectAll()) {
                System.out.println(n.getTitle());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
