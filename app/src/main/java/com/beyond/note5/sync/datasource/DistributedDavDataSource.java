package com.beyond.note5.sync.datasource;

import android.util.Log;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Document;
import com.beyond.note5.sync.webdav.DavLock;
import com.beyond.note5.sync.webdav.Lock;
import com.beyond.note5.sync.webdav.client.DavClient;
import com.beyond.note5.utils.OkWebDavUtil;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


public abstract class DistributedDavDataSource<T extends Document> extends DavDataSourceBase<T> {

    private static final String DOCUMENT_DIR = "/Note5/data/";

    private final DavClient client;

    private final ExecutorService executorService;

    private final String[] rootUrls;

    private final Lock lock;


    public DistributedDavDataSource(DavClient client, String... rootUrls) {
        this.client = client;
        this.rootUrls = rootUrls;
        this.executorService = MyApplication.getInstance().getExecutorService();
        this.lock = new DavLock(client, OkWebDavUtil.concat(rootUrls[0], DOCUMENT_DIR) + "distributeLock.lock");
    }

    public DistributedDavDataSource(DavClient client, ExecutorService executorService, String... rootUrls) {
        this.client = client;
        this.rootUrls = rootUrls;
        this.executorService = executorService;
        this.lock = new DavLock(client, OkWebDavUtil.concat(rootUrls[0], DOCUMENT_DIR) + "distributeLock.lock");
    }

    @Override
    public void add(T t) throws IOException {
        client.put(getDocumentUrl(rootUrls, t), encode(t));
    }

    @Override
    public void delete(T t) throws IOException {
        String documentUrl = getDocumentUrl(rootUrls, t);
        client.delete(documentUrl);
    }

    @Override
    public void update(T t) throws IOException {
        client.put(getDocumentUrl(rootUrls, t), encode(t));
    }

    @Override
    public T select(T t) throws IOException {
        return selectById(t.getId());
    }

    @Override
    public T selectById(String id) throws IOException {
        return decode(client.get(getDocumentUrl(rootUrls, id)));
    }

    @Override
    public List<T> selectAll() throws IOException {

        List<Future<T>> resultFutures = new ArrayList<>();
        for (String rootUrl : rootUrls) {
            List<String> ids = client.listAllFileName(OkWebDavUtil.concat(rootUrl, DOCUMENT_DIR));
            for (String id : ids) {
                Future<T> future = executorService.submit(new Callable<T>() {
                    @Override
                    public T call() throws Exception {
                        if (id.contains(".")) {
                            return null;
                        }
                        T t = clazz().newInstance();
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

    private String getDocumentUrl(String[] rootUrls, T t) {
        return getDocumentUrl(rootUrls, t.getId());
    }

    private String getDocumentUrl(String[] rootUrls, String id) {
        return OkWebDavUtil.concat(getRootUrl(rootUrls, id), getPath(id));
    }

    private String getPath(String id) {
        return DOCUMENT_DIR + id;
    }

    private String getRootUrl(String[] rootUrls, String id) {
        if (rootUrls.length == 0) {
            throw new RuntimeException("url不能为空");
        }
        if (rootUrls.length == 1) {
            if (rootUrls[0].endsWith("/")) {
                return StringUtils.substringBeforeLast(rootUrls[0], "/");
            }
            return rootUrls[0];
        }

        int index = Math.abs(id.hashCode()) % rootUrls.length;
        if (rootUrls[index].endsWith("/")) {
            return StringUtils.substringBeforeLast(rootUrls[index], "/");
        }
        return rootUrls[index];
    }

    private String encode(T t) {
        return JSONObject.toJSONString(t);
    }

    private T decode(String target) {
        if (target == null) {
            return null;
        }
        try {
            return JSONObject.parseObject(target, (Type) clazz());
        } catch (JSONException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<T> clazz() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        return (Class<T>) actualTypeArguments[0];
    }

    @Override
    protected Lock getLock() {
        return lock;
    }


    @Override
    public String[] getNodes() {
        return rootUrls;
    }

    @Override
    public String[] getPaths() {
        String[] paths = new String[1];
        paths[0] = DOCUMENT_DIR;
        return paths;
    }

    @Override
    public String getNode(T t) {
        return getRootUrl(rootUrls,t.getId());
    }

    @Override
    public String getPath(T t) {
        return getPath(t.getId());
    }
}
