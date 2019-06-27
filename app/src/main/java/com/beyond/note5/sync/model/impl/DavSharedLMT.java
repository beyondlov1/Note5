package com.beyond.note5.sync.model.impl;

import com.beyond.note5.sync.model.LMTSharedSource;
import com.beyond.note5.sync.webdav.client.DavClient;

import java.io.IOException;
import java.util.Date;

@Deprecated
public class DavSharedLMT implements LMTSharedSource {

    private DavClient client;

    private String url;

    public DavSharedLMT(DavClient client, String url) {
        this.client = client;
        this.url = url;
    }

    @Override
    public Date get() throws IOException {
        if (client.exists(url)){
            return new Date(Long.valueOf(client.get(url)));
        }else {
            return new Date(0);
        }
    }

    @Override
    public void set(Date date) throws IOException {
        client.put(url,date.getTime()+"");
    }
}
