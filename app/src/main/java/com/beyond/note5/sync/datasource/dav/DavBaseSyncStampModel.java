package com.beyond.note5.sync.datasource.dav;

import com.beyond.note5.sync.utils.SyncUtils;
import com.beyond.note5.sync.datasource.entity.SyncStamp;
import com.beyond.note5.sync.webdav.client.DavClient;
import com.beyond.note5.utils.OkWebDavUtil;

import java.io.IOException;
import java.util.Map;

/**
 * @author: beyond
 * @date: 2019/8/9
 */

public class DavBaseSyncStampModel extends AbstractDavSyncStampModel {

    public DavBaseSyncStampModel(DavClient client, DavDataSourceProperty property, Class clazz) {
        super(client, property, clazz);
    }

    @Override
    protected String getUrl(String oppositeKey) {
        String clazzUpCase = clazz.getSimpleName().toUpperCase();
        return OkWebDavUtil.concat(property.getServer(), clazzUpCase, property.getSyncStampPath(),
                property.getBaseSyncStampFilePrefix() + SyncUtils.base64Encode(oppositeKey) + ".stamp");
    }

    @Override
    public Map<String, SyncStamp> findAllConnectMe() throws IOException {
        throw new RuntimeException("not supported");
    }
}
