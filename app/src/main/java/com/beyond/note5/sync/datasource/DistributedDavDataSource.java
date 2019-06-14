package com.beyond.note5.sync.datasource;

import com.alibaba.fastjson.JSONObject;
import com.beyond.note5.bean.Document;
import com.beyond.note5.sync.DataSource;
import com.beyond.note5.utils.OkWebDavUtil;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;

public abstract class DistributedDavDataSource<T extends Document> implements DataSource<T> {

    private static final String DOCUMENT_DIR = "/NoteCloud2/data/";

    private final OkHttpClient client;

    private final String[] rootUrls;

    public DistributedDavDataSource(OkHttpClient client, String... rootUrls) {
        this.client = client;
        this.rootUrls = rootUrls;
    }

    @Override
    public void add(T t) {
        String url = getDocumentUrl(rootUrls, t);
        OkWebDavUtil.upload(client, url,JSONObject.toJSONString(t));
    }

    private String getDocumentUrl(String[] rootUrls, T t) {
        if (rootUrls.length == 0) {
            throw new RuntimeException("url不能为空");
        }
        if (rootUrls.length == 1) {
            if (rootUrls[0].endsWith("/")) {
                return StringUtils.substringBeforeLast(rootUrls[0], "/") + DOCUMENT_DIR + t.getId();
            }
            return rootUrls[0] + DOCUMENT_DIR + t.getId();
        }

        int index = RandomUtils.nextInt(0, rootUrls.length);
        if (rootUrls[index].endsWith("/")) {
            return StringUtils.substringBeforeLast(rootUrls[index], "/") + DOCUMENT_DIR + t.getId();
        }
        return rootUrls[index] + DOCUMENT_DIR + t.getId();
    }

    @Override
    public void delete(T t) {

    }

    @Override
    public void update(T t) {

    }

    @Override
    public T select(T t) {
        return null;
    }

    @Override
    public List<T> selectAll() throws IOException {
        return null;
    }

    @Override
    public void cover(List<T> all) throws IOException {

    }
}
