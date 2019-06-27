package com.beyond.note5.sync.model.impl;

import com.alibaba.fastjson.JSONObject;
import com.beyond.note5.sync.model.bean.SyncLogInfo;
import com.beyond.note5.sync.model.DavLogModel;
import com.beyond.note5.sync.webdav.client.DavClient;
import com.beyond.note5.utils.StringCompressUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DavLogModelImpl implements DavLogModel {

    private DavClient client;

    private String url;

    public DavLogModelImpl(DavClient client, String url) {
        this.client = client;
        this.url = url;
    }

    @Override
    public void saveAll(List<SyncLogInfo> syncLogInfos) throws IOException {
        client.put(url,StringCompressUtil.compress(JSONObject.toJSONString(syncLogInfos)));
    }

    @Override
    public List<SyncLogInfo> getAll() throws IOException {
        if (client.exists(url)){
            return JSONObject.parseArray(StringCompressUtil.unCompress(client.get(url)),SyncLogInfo.class);
        }else {
            return new ArrayList<>();
        }
    }
}
