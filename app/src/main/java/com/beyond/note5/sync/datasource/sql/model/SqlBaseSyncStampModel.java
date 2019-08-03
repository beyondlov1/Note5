package com.beyond.note5.sync.datasource.sql.model;

import com.beyond.note5.model.dao.LatestSyncStampDao;
import com.beyond.note5.sync.datasource.SyncStampModel;
import com.beyond.note5.sync.datasource.entity.LatestSyncStamp;
import com.beyond.note5.sync.datasource.entity.SyncStamp;
import com.beyond.note5.utils.IDUtil;

import java.io.IOException;

/**
 * @author: beyond
 * @date: 2019/8/3
 */

public class SqlBaseSyncStampModel implements SyncStampModel {

    private LatestSyncStampDao latestSyncStampDao;

    private String key1;

    private String key2;

    private String type;

    public SqlBaseSyncStampModel(LatestSyncStampDao latestSyncStampDao, String key1, String key2, String type) {
        this.latestSyncStampDao = latestSyncStampDao;
        this.key1 = key1;
        this.key2 = key2;
        this.type = type;
    }

    @Override
    public void update(SyncStamp syncStamp) throws IOException {
        LatestSyncStamp latestSyncStamp = getLatestSyncStamp();
        if (latestSyncStamp == null){
            LatestSyncStamp stamp = new LatestSyncStamp();
            stamp.setId(IDUtil.uuid());
            stamp.setLocalKey(key1);
            stamp.setRemoteKey(key2);
            stamp.setLastModifyTime(syncStamp.getLastModifyTime());
            stamp.setLastSyncTime(syncStamp.getLastSyncTimeEnd());
            stamp.setLastSyncTimeStart(syncStamp.getLastSyncTimeStart());
            stamp.setType(type);
            latestSyncStampDao.insert(stamp);
        } else {
            latestSyncStamp.setLastModifyTime(syncStamp.getLastModifyTime());
            latestSyncStamp.setLastSyncTime(syncStamp.getLastSyncTimeEnd());
            latestSyncStamp.setLastSyncTimeStart(syncStamp.getLastSyncTimeStart());
            latestSyncStampDao.update(latestSyncStamp);
        }
    }

    @Override
    public SyncStamp retrieve() throws IOException {
        LatestSyncStamp latestSyncStamp = getLatestSyncStamp();
        return latestSyncStamp == null ? SyncStamp.ZERO :
                SyncStamp.create(latestSyncStamp.getLastModifyTime(),
                        latestSyncStamp.getLastSyncTimeStart(),
                        latestSyncStamp.getLastSyncTime());
    }

    private LatestSyncStamp getLatestSyncStamp() {
        LatestSyncStamp latestSyncStamp = latestSyncStampDao.queryBuilder()
                .where(LatestSyncStampDao.Properties.LocalKey.eq(key1))
                .where(LatestSyncStampDao.Properties.RemoteKey.eq(key2))
                .where(LatestSyncStampDao.Properties.Type.eq(type))
                .unique();

        if (latestSyncStamp == null) {
            latestSyncStamp = latestSyncStampDao.queryBuilder()
                    .where(LatestSyncStampDao.Properties.LocalKey.eq(key2))
                    .where(LatestSyncStampDao.Properties.RemoteKey.eq(key1))
                    .where(LatestSyncStampDao.Properties.Type.eq(type))
                    .unique();
        }
        return latestSyncStamp;
    }
}
