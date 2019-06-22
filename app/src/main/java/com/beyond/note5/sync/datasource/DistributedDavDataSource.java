package com.beyond.note5.sync.datasource;

import android.util.Log;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.beyond.note5.bean.Document;
import com.beyond.note5.sync.model.LSTModel;
import com.beyond.note5.sync.webdav.Lock;
import com.beyond.note5.sync.webdav.client.DavClient;
import com.beyond.note5.utils.OkWebDavUtil;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


public class DistributedDavDataSource<T extends Document> implements DavDataSource<T> {

    private DavClient client;

    private ExecutorService executorService;

    private String server;

    private String[] paths;

    private Lock lock;

    private Class<T> clazz;

    private LSTModel LSTModel;

    private DistributedDavDataSource() {

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
    public List<T> selectAll() throws IOException {

        if (paths == null) {
            throw new RuntimeException("paths is null");
        }

        /**
         * 单线程方法
         */
        if (executorService == null) {
            List<T> result = new ArrayList<>();
            for (String path : paths) {
                List<String> ids = client.listAllFileName(OkWebDavUtil.concat(server, path));
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
            List<String> ids = client.listAllFileName(OkWebDavUtil.concat(server, path));
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

    @Override
    public void cover(List<T> all) {
        throw new RuntimeException("not available");
    }

    private String getDocumentUrl(T t) {
        return getDocumentUrl(t.getId());
    }

    private String getDocumentUrl(String id) {
        return OkWebDavUtil.concat(OkWebDavUtil.concat(server, getPathById(id)), id);
    }

    private String getPathById(String id) {
        if (paths.length == 0) {
            throw new RuntimeException("url不能为空");
        }
        if (paths.length == 1) {
            if (paths[0].endsWith("/")) {
                return StringUtils.substringBeforeLast(paths[0], "/");
            }
            return paths[0];
        }

        int index = Math.abs(id.hashCode()) % paths.length;
        if (paths[index].endsWith("/")) {
            return StringUtils.substringBeforeLast(paths[index], "/");
        }
        return paths[index];
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
    public Date getLastSyncTime() throws IOException {
        return LSTModel.getLastSyncTime();
    }

    @Override
    public void setLastSyncTime(Date date) throws IOException {
        LSTModel.setLastSyncTime(date);
    }


    public static class Builder<T extends Document> {
        private DavClient client;

        private ExecutorService executorService;

        private String server;

        private String[] paths;

        private Lock lock;

        private Class<T> clazz;

        private LSTModel recorder;

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

        public Builder<T> lstRecorder(LSTModel recorder) {
            this.recorder = recorder;
            return this;
        }

        public DavDataSource<T> build() {
            DistributedDavDataSource<T> davDataSource = new DistributedDavDataSource<T>();

            if (client == null || server == null || lock == null || executorService == null || clazz == null) {
                throw new RuntimeException("dav datasource build fail");
            }
            davDataSource.client = client;
            davDataSource.server = server;
            davDataSource.paths = paths;
            davDataSource.lock = lock;
            davDataSource.executorService = executorService;
            davDataSource.clazz = clazz;
            davDataSource.LSTModel = recorder;
            return davDataSource;
        }
    }
}
