package com.beyond.note5.sync.datasource.impl;

import android.util.Log;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.beyond.note5.bean.Document;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.DavDataSource;
import com.beyond.note5.sync.datasource.DavPathStrategy;
import com.beyond.note5.sync.model.SharedSource;
import com.beyond.note5.sync.model.bean.TraceInfo;
import com.beyond.note5.sync.webdav.Lock;
import com.beyond.note5.sync.webdav.client.DavClient;
import com.beyond.note5.sync.webdav.client.DavFilter;
import com.beyond.note5.sync.webdav.client.PostLastModifyTimeDavFilter;
import com.beyond.note5.utils.OkWebDavUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


public class DefaultDavDataSource<T extends Document> implements DavDataSource<T> {

    private DavClient client;

    private ExecutorService executorService;

    private String server;

    private String[] paths;

    private Lock lock;

    private Class<T> clazz;

    private SharedSource<TraceInfo> trace;

    private DavPathStrategy davPathStrategy;

    private DefaultDavDataSource() {

    }

    @Override
    public String getKey() {
        return server;
    }

    @Override
    public void add(T t) throws IOException {
        client.put(getDocumentUrl(t), encode(t));
    }

    @Override
    public void delete(T t) throws IOException {
        String documentUrl = getDocumentUrl(t);
        client.delete(documentUrl);
    }

    @Override
    public void update(T t) throws IOException {
        client.put(getDocumentUrl(t), encode(t));
    }

    @Override
    public T select(T t) throws IOException {
        return selectById(t.getId());
    }

    @Override
    public T selectById(String id) throws IOException {
        return decode(client.get(getDocumentUrl(id)));
    }

    @Override
    public List<T> selectByIds(List<String> ids) throws IOException {
        List<T> result = new ArrayList<>();
        for (String id : ids) {
            result.add(selectById(id));
        }
        return result;
    }

    public List<T> selectAllValid() throws IOException {
        List<T> result = new ArrayList<>();
        List<T> all = selectAll();
        if (all!=null){
            for (T t : all) {
                if (t.getValid()){
                    result.add(t);
                }
            }
        }
        return result;
    }

    @Override
    public List<T> selectAll() throws IOException {
        return selectByModifiedDate(null);
    }

    @Override
    public void cover(List<T> all) {
        throw new RuntimeException("not available");
    }

    @Override
    public Class<T> clazz() {
        return clazz;
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
        return paths;
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
    public TraceInfo getTraceInfo(DataSource<T> targetDataSource) throws IOException {
        return trace.get();
    }

    @Override
    public void setTraceInfo(TraceInfo traceInfo, DataSource<T> targetDataSource) throws IOException {
        trace.set(traceInfo);
    }


    @Override
    public DavPathStrategy getPathStrategy() {
        return davPathStrategy;
    }

    @Override
    public List<T> selectByModifiedDate(Date date) throws IOException {
        if (paths == null) {
            throw new RuntimeException("paths is null");
        }

        DavFilter davFilter = null;
        if (date != null) {
            davFilter = new PostLastModifyTimeDavFilter(date);
        }
        /**
         * 单线程方法
         */
        if (executorService == null) {
            List<T> result = new ArrayList<>();
            for (String path : paths) {
                List<String> ids = client.listAllFileName(OkWebDavUtil.concat(server, path), davFilter);
                for (String id : ids) {
                    if (id.contains(".")) {
                        return null;
                    }
                    try {
                        T t = clazz.newInstance();
                        t.setId(id);
                        result.add(select(t));
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            return result;
        }

        /**
         * 多线程方法
         */
        List<Future<T>> resultFutures = new ArrayList<>();
        for (String path : paths) {
            List<String> ids = client.listAllFileName(OkWebDavUtil.concat(server, path), davFilter);
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

    public static class Builder<T extends Document> {
        private DavClient client;

        private ExecutorService executorService;

        private String server;

        private String[] paths;

        private Lock lock;

        private Class<T> clazz;

        private SharedSource<TraceInfo> trace;

        private DavPathStrategy davPathStrategy;

        public Builder<T> davClient(DavClient client) {
            this.client = client;
            return this;
        }

        public Builder<T> executorService(ExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }

        public Builder<T> server(String server) {
            this.server = server;
            return this;
        }

        public Builder<T> paths(String... paths) {
            this.paths = paths;
            return this;
        }

        public Builder<T> lock(Lock lock) {
            this.lock = lock;
            return this;
        }

        public Builder<T> clazz(Class<T> clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder<T> sharedSource(SharedSource<TraceInfo> trace) {
            this.trace = trace;
            return this;
        }

        public Builder<T> davPathStrategy(DavPathStrategy davPathStrategy){
            this.davPathStrategy = davPathStrategy;
            return this;
        }

        public DefaultDavDataSource<T> build() {
            DefaultDavDataSource<T> davDataSource = new DefaultDavDataSource<T>();

            if (client == null || server == null || lock == null || clazz == null) {
                throw new RuntimeException("dav datasource build fail");
            }
            davDataSource.client = client;
            davDataSource.server = server;
            davDataSource.paths = paths;
            davDataSource.lock = lock;
            davDataSource.executorService = executorService;
            davDataSource.clazz = clazz;
            davDataSource.trace = trace;
            if (davPathStrategy == null){
                davDataSource.davPathStrategy = new DefaultDavPathStrategy(server,paths);
            }
            return davDataSource;
        }
    }
}
