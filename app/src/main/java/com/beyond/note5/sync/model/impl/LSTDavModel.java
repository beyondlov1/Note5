package com.beyond.note5.sync.model.impl;

import com.beyond.note5.sync.model.LSTModel;
import com.beyond.note5.sync.webdav.client.DavClient;

import java.io.IOException;
import java.util.Date;

public class LSTDavModel implements LSTModel {

    private DavClient client;

    private String url;

    public LSTDavModel(DavClient client, String url) {
        this.client = client;
        this.url = url;
    }

    @Override
    public Date getLastSyncTime() throws IOException {
        if (client.exists(url)){
            return new Date(Long.valueOf(client.get(url)));
        }else {
            return new Date(0);
        }
    }

    @Override
    public void setLastSyncTime(Date date) throws IOException {
        client.put(url,date.getTime()+"");
    }
}
