package com.beyond.note5.sync.datasource;

import com.alibaba.fastjson.JSONObject;
import com.beyond.note5.bean.Document;
import com.beyond.note5.sync.webdav.DavLock;
import com.beyond.note5.sync.webdav.Lock;
import com.beyond.note5.sync.webdav.client.DavClient;
import com.beyond.note5.utils.StringCompressUtil;

import java.io.IOException;
import java.util.List;

public abstract class SingleDavDataSource<T extends Document> extends DavDataSourceBase<T> {

    private final DavClient client;

    protected String url;

    private final Lock lock;

    public SingleDavDataSource(DavClient client, String url) {
        this.url = url;
        this.client = client;
        this.lock = new DavLock(client, url + ".lock");
    }

    @Override
    public void add(T document) {
        throw new RuntimeException("暂不支持");
    }

    @Override
    public void delete(T document) {
        throw new RuntimeException("暂不支持");
    }

    @Override
    public void update(T document) {
        throw new RuntimeException("暂不支持");
    }

    @Override
    public T select(T document) {
        throw new RuntimeException("暂不支持");
    }

    @Override
    public T selectById(String id) throws IOException {
        throw new RuntimeException("暂不支持");
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> selectAll() throws IOException {
        if (client.exists(getDownloadUrl())) {
            String data = client.get(getDownloadUrl());
            return JSONObject.parseArray(StringCompressUtil.unCompress(data), clazz());
        }else {
            client.put(getDownloadUrl(),"");
        }
        return null;
    }

    @Override
    public void cover(List<T> all) throws IOException {
        String jsonString = JSONObject.toJSONString(all);
        client.put(getUploadUrl(), StringCompressUtil.compress(jsonString));
    }

    @Override
    public abstract Class<T> clazz();

    public String getDownloadUrl() {
        return url;
    }

    public String getUploadUrl() {
        return url;
    }

    @Override
    protected Lock getLock() {
        return lock;
    }
}
