package com.beyond.note5.sync.datasource.dav;

import android.support.annotation.NonNull;
import android.util.Log;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.beyond.note5.bean.Tracable;
import com.beyond.note5.sync.context.SyncContext;
import com.beyond.note5.sync.context.model.SyncStateEnum;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.SyncStampModel;
import com.beyond.note5.sync.datasource.entity.SyncStamp;
import com.beyond.note5.sync.exception.SaveException;
import com.beyond.note5.sync.webdav.DavLock;
import com.beyond.note5.sync.webdav.Lock;
import com.beyond.note5.sync.webdav.client.AfterModifiedTimeDavFilter;
import com.beyond.note5.sync.webdav.client.DavClient;
import com.beyond.note5.sync.webdav.client.DavFilter;
import com.beyond.note5.utils.OkWebDavUtil;

import org.apache.commons.lang3.time.DateUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.beyond.note5.MyApplication.DAV_LOCK_DIR;
import static com.beyond.note5.MyApplication.DAV_STAMP_BASE_PREFIX;
import static com.beyond.note5.MyApplication.DAV_STAMP_DIR;
import static com.beyond.note5.MyApplication.DAV_STAMP_LATEST_NAME;


public class DefaultDavDataSource<T extends Tracable> implements DavDataSource<T> {

    private final String oppositeKey;

    private DavClient client;

    private String server;

    private SyncStampModel baseSyncStampModel;

    private SyncStampModel latestSyncStampModel;

    private DavPathStrategy davPathStrategy;

    private ExecutorService executorService;

    private Lock lock;

    private Class<T> clazz;

    private SyncContext context;

    public DefaultDavDataSource(String oppositeKey,
                                DavClient client,
                                String server,
                                DavPathStrategy pathStrategy,
                                ExecutorService executorService,
                                Class<T> clazz) {
        this.oppositeKey = oppositeKey;
        this.client = client;
        this.server = server;
        this.executorService = executorService;
        this.clazz = clazz;
        this.baseSyncStampModel = new DavSyncStampModel(client, server, getRemoteBaseSyncStampPath());
        this.latestSyncStampModel = new DavSyncStampModel(client, server, getRemoteLatestSyncStampPath());
        this.davPathStrategy = pathStrategy;
        this.lock = new DavLock(client, getLockUrl(server));
    }

    private String getLockUrl(String server) {
        String clazzUpCase = clazz.getSimpleName().toUpperCase();
        return OkWebDavUtil.concat(server, clazzUpCase, DAV_LOCK_DIR, clazzUpCase + ".lock");
    }

    private String getRemoteLatestSyncStampPath() {
        String clazzUpCase = clazz.getSimpleName().toUpperCase();
        return OkWebDavUtil.concat(clazzUpCase, DAV_STAMP_DIR, DAV_STAMP_LATEST_NAME+".stamp");
    }

    private String getRemoteBaseSyncStampPath() {
        String clazzUpCase = clazz.getSimpleName().toUpperCase();
        return OkWebDavUtil.concat(clazzUpCase, DAV_STAMP_DIR, DAV_STAMP_BASE_PREFIX + oppositeKey + ".stamp");
    }

    @Override
    public String getKey() {
        return server;
    }

    @Override
    public void saveAll(List<T> ts) throws IOException, SaveException {
        saveAll(ts, oppositeKey);
    }

    @Override
    public void saveAll(List<T> ts, String source) throws IOException, SaveException {
        if (executorService == null) {
            singleThreadSaveAll(ts);
            return;
        }
        multiThreadSaveAll(ts);
    }

    private void multiThreadSaveAll(List<T> ts) throws SaveException {
        mkDirForMultiThread();
        CompletionService<T> completionService = new ExecutorCompletionService<>(executorService);
        for (T t : ts) {
            completionService.submit(new Callable<T>() {
                @Override
                public T call() throws Exception {
                    save(t);
                    return t;
                }
            });
        }

        List<T> result = new ArrayList<>();
        try {
            for (int i = 0; i < ts.size(); i++) {
                Future<T> future = completionService.take();
                T t = future.get();
                if (t != null) {
                    result.add(t);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Log.e(getClass().getSimpleName(), "", e);
            List<String> successIds = new ArrayList<>(result.size());
            for (T t : result) {
                successIds.add(t.getId());
            }
            throw new SaveException(e, getKey(), successIds);
        }
    }

    private void mkDirForMultiThread() {
        String[] paths = getPaths();
        for (String path : paths) {
            getClient().mkDirQuietly(OkWebDavUtil.concat(getServer(), path));
            getClient().mkDirQuietly(OkWebDavUtil.concat(getServer(), path, "FILES"));
        }
    }

    private void singleThreadSaveAll(List<T> ts) throws SaveException {
        List<String> successIds = new ArrayList<>();
        for (T t : ts) {
            try {
                save(t);
                successIds.add(t.getId());
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "save失败", e);
                throw new SaveException(e, getKey(), successIds);
            }
        }
    }

    private void save(T t) throws IOException {
        if (client.exists(getDocumentUrl(t))) {
            T remoteT = decode(client.get(getDocumentUrl(t)));
            if (t.getLastModifyTime().after(remoteT.getLastModifyTime())
                    || (t.getVersion() == null ? 0 : t.getVersion()) > (remoteT.getVersion() == null ? 0 : remoteT.getVersion())) {
                update(t);
            }
        } else {
            add(t);
        }
        context.saveSyncState(t.getId(), SyncStateEnum.SUCCESS);
    }

    protected void add(T t) throws IOException {
        client.put(getDocumentUrl(t), encode(t));
    }

    protected void update(T t) throws IOException {
        client.put(getDocumentUrl(t), encode(t));
    }

    @Override
    public List<T> selectAll() throws IOException {
        return selectByModifiedDate(null);
    }

    @Override
    public boolean isChanged(DataSource<T> targetDataSource) throws IOException {
        SyncStamp baseSyncStamp = baseSyncStampModel.retrieve();
        SyncStamp latestSyncStamp = getLatestSyncStamp();

        Date correspondLastModifyTime = baseSyncStamp.getLastModifyTime();
        Date latestLastModifyTime = latestSyncStamp.getLastModifyTime();
        Date correspondLastSyncTimeEnd = baseSyncStamp.getLastSyncTimeEnd();
        Date latestLastSyncTimeEnd = latestSyncStamp.getLastSyncTimeEnd();

        return !(DateUtils.isSameInstant(correspondLastModifyTime, latestLastModifyTime)
                && DateUtils.isSameInstant(correspondLastSyncTimeEnd, latestLastSyncTimeEnd));
    }

    @Override
    public List<T> getChangedData(SyncStamp syncStamp) throws IOException {
        return selectByModifiedDate(syncStamp.getLastSyncTimeEnd());
    }

    private List<T> selectByModifiedDate(Date date) throws IOException {

        DavFilter davFilter = null;
        if (date != null) {
            davFilter = new AfterModifiedTimeDavFilter(date);
        }

        List<String> modifiedIds = getModifiedIds(davFilter);
        /**
         * 单线程方法
         */
        if (executorService == null) {
            return singleThreadRetrieve(modifiedIds);
        }

        /**
         * 多线程方法
         */
        return multiThreadRetrieve(modifiedIds);
    }

    @NonNull
    private List<T> multiThreadRetrieve(List<String> ids) throws IOException {
        List<Future<T>> resultFutures = new ArrayList<>();
        for (String id : ids) {
            Future<T> future = executorService.submit(new Callable<T>() {
                @Override
                public T call() throws Exception {
                    if (id.contains(".")) {
                        return null;
                    }
                    T t = clazz.newInstance();
                    t.setId(id);
                    return select(t);
                }
            });
            resultFutures.add(future);
        }

        List<T> result = new ArrayList<>();
        try {
            for (Future<T> future : resultFutures) {
                T t = future.get();
                if (t != null) {
                    result.add(t);
                }
            }
            return result;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Log.e(getClass().getSimpleName(), "", e);
        }
        return result;
    }

    @NonNull
    private List<T> singleThreadRetrieve(List<String> ids) throws IOException {
        List<T> result = new ArrayList<>();
        for (String id : ids) {
            if (id.contains(".")) {
                continue;
            }
            try {
                T t = clazz.newInstance();
                t.setId(id);
                result.add(select(t));
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private T select(T t) throws IOException {
        return selectById(t.getId());
    }

    private T selectById(String id) throws IOException {
        return decode(client.get(getDocumentUrl(id)));
    }

    @Override
    public Class<T> clazz() {
        return clazz;
    }

    @Override
    public void setContext(SyncContext context) {
        this.context = context;
    }

    @Override
    public SyncStamp getLastSyncStamp(DataSource<T> targetDataSource) throws IOException {
        return baseSyncStampModel.retrieve();
    }

    @Override
    public void updateLastSyncStamp(SyncStamp syncStamp, DataSource<T> targetDataSource) throws IOException {
        baseSyncStampModel.update(syncStamp);
    }

    @Override
    public SyncStamp getLatestSyncStamp() throws IOException {
        return latestSyncStampModel.retrieve();
    }

    @Override
    public void updateLatestSyncStamp(SyncStamp syncStamp) throws IOException {
        latestSyncStampModel.update(syncStamp);
    }

    private String getDocumentUrl(T t) {
        return getDocumentUrl(t.getId());
    }

    private String getDocumentUrl(String id) {
        return OkWebDavUtil.concat(OkWebDavUtil.concat(server, getPathById(id)), id);
    }

    private String getPathById(String id) {
        return davPathStrategy.getStoragePath(id);
    }

    private String encode(T t) {
        return JSONObject.toJSONString(t);
    }

    private T decode(String target) {
        if (target == null) {
            return null;
        }
        try {
            return JSONObject.parseObject(target, (Type) clazz);
        } catch (JSONException e) {
            return null;
        }
    }


    @Override
    public boolean tryLock(Long time) {
        return lock.tryLock(time);
    }

    @Override
    public boolean isLocked() {
        return lock.isLocked();
    }

    @Override
    public boolean tryLock() {
        return lock.tryLock();
    }

    @Override
    public boolean release() {
        return lock.release();
    }


    @Override
    public String getServer() {
        return server;
    }

    @Override
    public String[] getPaths() {
        return davPathStrategy.getAllStoragePaths(clazz.getSimpleName().toLowerCase());
    }

    @Override
    public String getPath(T t) {
        return getPathById(t.getId());
    }

    @Override
    public DavClient getClient() {
        return client;
    }

    @Override
    public DavPathStrategy getPathStrategy() {
        return davPathStrategy;
    }

    @NonNull
    private List<String> getModifiedIds(DavFilter davFilter) throws IOException {
        List<String> ids = new ArrayList<>();
        String[] allPaths = davPathStrategy.getAllStoragePaths(clazz.getSimpleName().toLowerCase());
        for (String path : allPaths) {
            ids.addAll(client.listAllFileName(OkWebDavUtil.concat(server, path), davFilter));
        }
        return ids;
    }

    @Override
    public void upload(String url, String path) throws IOException {
        getClient().upload(path, url);
    }

    @Override
    public void download(String url, String path) throws IOException {
        getClient().download(url, path);
    }
}
