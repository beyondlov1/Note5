package com.beyond.note5.sync.model.impl;

import com.alibaba.fastjson.JSONObject;
import com.beyond.note5.sync.model.SharedSource;
import com.beyond.note5.sync.model.entity.TraceInfo;
import com.beyond.note5.sync.webdav.client.DavClient;


import java.io.IOException;

public class DavSharedTraceInfo implements SharedSource<TraceInfo> {
    private DavClient client;

    private String url;

    public DavSharedTraceInfo(DavClient client, String url) {
        this.client = client;
        this.url = url;
    }

    @Override
    public TraceInfo get() throws IOException {
        if (client.exists(url)){
            return JSONObject.parseObject(client.get(url), TraceInfo.class);
        }else {
            return TraceInfo.ZERO;
        }
    }

    @Override
    public void set(TraceInfo traceInfo) throws IOException {
        client.put(url,JSONObject.toJSONString(traceInfo));
    }
}
