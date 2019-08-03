package com.beyond.note5.sync.model.impl;

import com.alibaba.fastjson.JSONObject;
import com.beyond.note5.sync.model.SharedSource;
import com.beyond.note5.sync.datasource.entity.SyncStamp;
import com.beyond.note5.sync.webdav.client.DavClient;


import java.io.IOException;

public class DavSharedTraceInfo implements SharedSource<SyncStamp> {
    private DavClient client;

    private String url;

    public DavSharedTraceInfo(DavClient client, String url) {
        this.client = client;
        this.url = url;
    }

    @Override
    public SyncStamp get() throws IOException {
        if (client.exists(url)){
            return JSONObject.parseObject(client.get(url), SyncStamp.class);
        }else {
            return SyncStamp.ZERO;
        }
    }

    @Override
    public void set(SyncStamp syncStamp) throws IOException {
        client.put(url,JSONObject.toJSONString(syncStamp));
    }
}
