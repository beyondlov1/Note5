package com.beyond.note5.sync.datasource;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.beyond.note5.bean.Document;
import com.beyond.note5.sync.DataSource;
import com.beyond.note5.utils.OkWebDavUtil;
import com.beyond.note5.utils.StringCompressUtil;

import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public abstract class DavDataSource<T extends Document> implements DataSource<T> {

    protected String url;

    public DavDataSource(String url) {
        this.url = url;
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

    @SuppressWarnings("unchecked")
    @Override
    public List<T> selectAll() throws IOException {
        String data = download(getDownloadUrl());
        return JSONObject.parseArray(StringCompressUtil.unCompress(data),clazz());
    }

    @Override
    public void cover(List<T> all) throws IOException {
        String jsonString = JSONObject.toJSONString(all);
        upload(getUploadUrl(), StringCompressUtil.compress(jsonString));
    }

    @Override
    public abstract Class clazz();

    private void upload(String url, String content) {

        final Request request = new Request.Builder()
                .url(url)
                .method("PUT",RequestBody.create(MediaType.get("application/x-www-form-urlencoded"),content.getBytes()))
                .build();
        try ( Response mkResponse = OkWebDavUtil.mkRemoteDir(url);Response response = OkWebDavUtil.requestForResponse(request)) {
            if (!mkResponse.isSuccessful()){
                throw new RuntimeException("创建文件夹失败");
            }

            if (!response.isSuccessful()) {
                throw new RuntimeException("上传失败");
            }
        } catch (Exception e) {
            Log.e("dav", "request fail");
            throw new RuntimeException("上传失败");
        }

    }

    private String download(String url) throws IOException {
        final Request request = new Request.Builder()
                .url(url)
                .method("GET",null)
                .build();

        return OkWebDavUtil.requestForString(request, new OkWebDavUtil.Callback<String, String>() {
            @Override
            public String onSuccess(String s) {
                return s;
            }

            @Override
            public void onFail() {
                upload(url, "");
            }
        });
    }

    public String getDownloadUrl() {
        return url;
    }

    public String getUploadUrl() {
        return url;
    }
}
