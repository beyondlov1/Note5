package com.beyond.note5.sync.datasource.dav;

import com.beyond.note5.sync.datasource.entity.SyncStamp;
import com.beyond.note5.sync.webdav.client.DavClient;
import com.beyond.note5.utils.OkWebDavUtil;

import java.util.Map;

/**
 * @author: beyond
 * @date: 2019/8/9
 */

public class DavLatestSyncStampModel extends AbstractDavSyncStampModel {

    public DavLatestSyncStampModel(DavClient client, DavDataSourceProperty property, Class clazz) {
        super(client, property, clazz);
    }

    @Override
    protected String getUrl(String oppositeKey) {
        String clazzUpCase = clazz.getSimpleName().toUpperCase();
        return OkWebDavUtil.concat(property.getServer(),
                clazzUpCase, property.getSyncStampPath(), property.getLatestSyncStampFileName()+".stamp");
    }

    @Override
    public Map<String, SyncStamp> findAllConnectMe() {
        throw new RuntimeException("not supported");
    }
}
