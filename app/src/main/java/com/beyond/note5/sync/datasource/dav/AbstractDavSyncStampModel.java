package com.beyond.note5.sync.datasource.dav;

import com.alibaba.fastjson.JSONObject;
import com.beyond.note5.sync.datasource.SyncStampModel;
import com.beyond.note5.sync.datasource.entity.SyncStamp;
import com.beyond.note5.sync.webdav.client.DavClient;

import java.io.IOException;

/**
 * @author: beyond
 * @date: 2019/8/3
 */

public abstract class AbstractDavSyncStampModel implements SyncStampModel {

    protected DavClient client;

    protected DavDataSourceProperty property;

    protected Class clazz;

    public AbstractDavSyncStampModel(DavClient client, DavDataSourceProperty property, Class clazz) {
        this.client = client;
        this.property = property;
        this.clazz = clazz;
    }

    @Override
    public void update(SyncStamp syncStamp,String oppositeKey) throws IOException {
        client.put(getUrl(oppositeKey), JSONObject.toJSONString(syncStamp));
    }

    @Override
    public SyncStamp retrieve( String oppositeKey) throws IOException {
        if (client.exists(getUrl(oppositeKey))) {
            return JSONObject.parseObject(client.get(getUrl(oppositeKey)), SyncStamp.class);
        } else {
            return SyncStamp.ZERO;
        }
    }

    protected abstract String getUrl(String oppositeKey);
}
