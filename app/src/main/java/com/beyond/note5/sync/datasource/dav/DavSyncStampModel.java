package com.beyond.note5.sync.datasource.dav;

import com.alibaba.fastjson.JSONObject;
import com.beyond.note5.sync.datasource.SyncStampModel;
import com.beyond.note5.sync.datasource.entity.SyncStamp;
import com.beyond.note5.sync.webdav.client.DavClient;
import com.beyond.note5.utils.OkWebDavUtil;

import java.io.IOException;

/**
 * @author: beyond
 * @date: 2019/8/3
 */

public class DavSyncStampModel implements SyncStampModel {

    private DavClient client;

    private String url;

    public DavSyncStampModel(DavClient client, String server, String path) {
        this.client = client;
        this.url = OkWebDavUtil.concat(server, path);
    }

    @Override
    public void update(SyncStamp syncStamp) throws IOException {
        client.put(url, JSONObject.toJSONString(syncStamp));
    }

    @Override
    public SyncStamp retrieve() throws IOException {
        if (client.exists(url)) {
            return JSONObject.parseObject(client.get(url), SyncStamp.class);
        } else {
            return SyncStamp.ZERO;
        }
    }
}
