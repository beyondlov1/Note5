package com.beyond.note5.sync.datasource.dav;

import android.support.annotation.NonNull;
import android.util.Log;

import com.beyond.note5.bean.Tracable;
import com.beyond.note5.sync.context.entity.SyncState;
import com.beyond.note5.sync.context.model.SyncStateEnum;
import com.beyond.note5.sync.context.model.SyncStateModel;
import com.beyond.note5.sync.context.model.SyncStateModelImpl;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.SyncStampModel;
import com.beyond.note5.sync.datasource.entity.SyncStamp;
import com.beyond.note5.sync.exception.SaveException;
import com.beyond.note5.sync.utils.JsonSerializer;
import com.beyond.note5.sync.utils.Serializer;
import com.beyond.note5.sync.webdav.DavLock;
import com.beyond.note5.sync.webdav.Lock;
import com.beyond.note5.sync.webdav.client.AfterModifiedTimeDavFilter;
import com.beyond.note5.sync.webdav.client.DavClient;
import com.beyond.note5.sync.webdav.client.DavFilter;
import com.beyond.note5.sync.webdav.client.SardineDavClient;
import com.beyond.note5.utils.OkWebDavUtil;

import org.apache.commons.lang3.time.DateUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class DefaultPointDavDataSource<T extends Tracable> implements DavDataSource<T> {

    private DavClient client;

    private SyncStampModel baseSyncStampModel;

    private SyncStampModel latestSyncStampModel;

    private DavPathStrategy davPathStrategy;

    private ExecutorService executorService;

    private Lock lock;

    private SyncStateModel syncStateModel;

    private Serializer<String, T> serializer;

    protected Class<T> clazz;

    protected DavDataSourceProperty property;

    public DefaultPointDavDataSource(DavDataSourceProperty property, Class<T> clazz) {
        this.property = property;
        this.clazz = clazz;
    }

    @Override
    public void init() {
        this.client = new SardineDavClient(property.getUsername(), property.getPassword());
        if (property.isNeedExecutorService()) {
            executorService = new ThreadPoolExecutor(
                    17, 60,
                    60, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>());
        }
        this.baseSyncStampModel = new DavBaseSyncStampModel(client, property, clazz);
        this.latestSyncStampModel = new DavLatestSyncStampModel(client, property, clazz);
        this.davPathStrategy = new UuidDavPathStrategy(property.getServer(), clazz);
        this.lock = new DavLock(client, getLockUrl(property.getServer()), getKey());
        this.syncStateModel = new SyncStateModelImpl();
        this.serializer = new JsonSerializer<>(clazz);
    }

    private String getLockUrl(String server) {
        String clazzUpCase = clazz.getSimpleName().toUpperCase();
        return OkWebDavUtil.concat(server, clazzUpCase, property.getLockPath(), clazzUpCase + ".lock");
    }

    @Override
    public String getKey() {
        return property.getServer();
    }

    @Override
    public void saveAll(List<T> ts, String... oppositeKeys) throws IOException, SaveException {
        if (executorService == null) {
            singleThreadSaveAll(ts, oppositeKeys);
            return;
        }
        multiThreadSaveAll(ts, oppositeKeys);
    }

    private void multiThreadSaveAll(List<T> ts, String[] oppositeKeys) throws SaveException {
        mkDirForMultiThread();
        CompletionService<T> completionService = new ExecutorCompletionService<>(executorService);
        for (T t : ts) {
            completionService.submit(new Callable<T>() {
                @Override
                public T call() throws Exception {
                    save(t, oppositeKeys);
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

    private void singleThreadSaveAll(List<T> ts, String[] oppositeKey) throws SaveException {
        List<String> successIds = new ArrayList<>();
        for (T t : ts) {
            try {
                save(t, oppositeKey);
                successIds.add(t.getId());
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "save失败", e);
                throw new SaveException(e, getKey(), successIds);
            }
        }
    }

    private void save(T t, String[] oppositeKeys) throws IOException {
        if (client.exists(getDocumentUrl(t))) {
            T remoteT = decode(client.get(getDocumentUrl(t)));
            if (t.getLastModifyTime().after(remoteT.getLastModifyTime())
                    || (t.getVersion() == null ? 0 : t.getVersion()) > (remoteT.getVersion() == null ? 0 : remoteT.getVersion())) {
                update(t);
            }
        } else {
            add(t);
        }

        saveSuccessState(t, oppositeKeys);
    }

    protected void saveSuccessState(T t, String[] oppositeKeys) {

        List<SyncState> syncStateInfos = new ArrayList<>(oppositeKeys.length);
        for (String oppositeKey : oppositeKeys) {
            SyncState syncStateInfo = SyncState.create();
            syncStateInfo.setDocumentId(t.getId());
            syncStateInfo.setLocal(oppositeKey);
            syncStateInfo.setServer(getKey());
            syncStateInfo.setState(SyncStateEnum.SUCCESS.getValue());
            syncStateInfo.setType(clazz().getSimpleName().toLowerCase());
            syncStateInfos.add(syncStateInfo);
        }

        syncStateModel.saveAll(syncStateInfos);
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
        SyncStamp baseSyncStamp = baseSyncStampModel.retrieve(targetDataSource.getKey());
        SyncStamp latestSyncStamp = getLatestSyncStamp();

        Date correspondLastModifyTime = baseSyncStamp.getLastModifyTime();
        Date latestLastModifyTime = latestSyncStamp.getLastModifyTime();
        Date correspondLastSyncTimeEnd = baseSyncStamp.getLastSyncTimeEnd();
        Date latestLastSyncTimeEnd = latestSyncStamp.getLastSyncTimeEnd();

        return !(DateUtils.isSameInstant(correspondLastModifyTime, latestLastModifyTime)
                && DateUtils.isSameInstant(correspondLastSyncTimeEnd, latestLastSyncTimeEnd));
    }

    @Override
    public List<T> getChangedData(SyncStamp syncStamp, DataSource<T> targetDataSource) throws IOException {
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
    public SyncStamp getLastSyncStamp(DataSource<T> targetDataSource) throws IOException {
        return baseSyncStampModel.retrieve(targetDataSource.getKey());
    }

    @Override
    public void updateLastSyncStamp(SyncStamp syncStamp, DataSource<T> targetDataSource) throws IOException {
        baseSyncStampModel.update(syncStamp, targetDataSource.getKey());
    }

    @Override
    public SyncStamp getLatestSyncStamp() throws IOException {
        return latestSyncStampModel.retrieve(null);
    }

    @Override
    public void updateLatestSyncStamp(SyncStamp syncStamp) throws IOException {
        latestSyncStampModel.update(syncStamp, null);
    }

    private String getDocumentUrl(T t) {
        return getDocumentUrl(t.getId());
    }

    private String getDocumentUrl(String id) {
        return OkWebDavUtil.concat(OkWebDavUtil.concat(property.getServer(), getPathById(id)), id);
    }

    private String getPathById(String id) {
        return davPathStrategy.getStoragePath(id);
    }

    private String encode(T t) {
        return serializer.encode(t);
    }

    private T decode(String target) {
        return serializer.decode(target);
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
        return property.getServer();
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
            ids.addAll(client.listAllFileName(OkWebDavUtil.concat(property.getServer(), path), davFilter));
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
