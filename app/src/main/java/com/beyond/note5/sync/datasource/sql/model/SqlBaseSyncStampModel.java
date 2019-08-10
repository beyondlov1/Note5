package com.beyond.note5.sync.datasource.sql.model;

import com.beyond.note5.model.dao.BaseSyncStampDao;
import com.beyond.note5.sync.datasource.SyncStampModel;
import com.beyond.note5.sync.datasource.entity.BaseSyncStamp;
import com.beyond.note5.sync.datasource.entity.SyncStamp;
import com.beyond.note5.utils.IDUtil;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: beyond
 * @date: 2019/8/3
 */

public class SqlBaseSyncStampModel implements SyncStampModel {

    private BaseSyncStampDao baseSyncStampDao;

    private String thisKey;

    private String type;

    public SqlBaseSyncStampModel(BaseSyncStampDao baseSyncStampDao, String thisKey, String type) {
        this.baseSyncStampDao = baseSyncStampDao;
        this.thisKey = thisKey;
        this.type = type;
    }

    @Override
    public void update(SyncStamp syncStamp,String oppositeKey) throws IOException {
        BaseSyncStamp baseSyncStamp = getBaseSyncStamp(thisKey,oppositeKey);
        if (baseSyncStamp == null){
            BaseSyncStamp stamp = new BaseSyncStamp();
            stamp.setId(IDUtil.uuid());
            stamp.setLocalKey(thisKey);
            stamp.setRemoteKey(oppositeKey);
            stamp.setLastModifyTime(syncStamp.getLastModifyTime());
            stamp.setLastSyncTime(syncStamp.getLastSyncTimeEnd());
            stamp.setLastSyncTimeStart(syncStamp.getLastSyncTimeStart());
            stamp.setType(type);
            baseSyncStampDao.insert(stamp);
        } else {
            baseSyncStamp.setLastModifyTime(syncStamp.getLastModifyTime());
            baseSyncStamp.setLastSyncTime(syncStamp.getLastSyncTimeEnd());
            baseSyncStamp.setLastSyncTimeStart(syncStamp.getLastSyncTimeStart());
            baseSyncStampDao.update(baseSyncStamp);
        }
    }

    @Override
    public SyncStamp retrieve(String oppositeKey) throws IOException {
        BaseSyncStamp baseSyncStamp = getBaseSyncStamp(thisKey,oppositeKey);
        return baseSyncStamp == null ? SyncStamp.ZERO :
                SyncStamp.create(baseSyncStamp.getLastModifyTime(),
                        baseSyncStamp.getLastSyncTimeStart(),
                        baseSyncStamp.getLastSyncTime());
    }

    @Override
    public Map<String, SyncStamp> findAllConnectMe() throws IOException {
        List<BaseSyncStamp> baseSyncStamps = baseSyncStampDao.queryBuilder()
                .where(BaseSyncStampDao.Properties.LocalKey.eq(thisKey))
                .where(BaseSyncStampDao.Properties.Type.eq(type))
                .list();

        Map<String,SyncStamp> syncStamps = new LinkedHashMap<>();
        for (BaseSyncStamp baseSyncStamp : baseSyncStamps) {
            syncStamps.put(baseSyncStamp.getRemoteKey(),
                    SyncStamp.create(baseSyncStamp.getLastModifyTime(),
                            baseSyncStamp.getLastSyncTimeStart(),
                            baseSyncStamp.getLastSyncTime()));
        }

        List<BaseSyncStamp> list2 = baseSyncStampDao.queryBuilder()
                .where(BaseSyncStampDao.Properties.RemoteKey.eq(thisKey))
                .where(BaseSyncStampDao.Properties.Type.eq(type))
                .list();
        for (BaseSyncStamp baseSyncStamp : list2) {
            syncStamps.put(baseSyncStamp.getRemoteKey(),
                    SyncStamp.create(baseSyncStamp.getLastModifyTime(),
                            baseSyncStamp.getLastSyncTimeStart(),
                            baseSyncStamp.getLastSyncTime()));
        }
        return syncStamps;
    }

    private BaseSyncStamp getBaseSyncStamp(String key1, String key2) {
        BaseSyncStamp baseSyncStamp = baseSyncStampDao.queryBuilder()
                .where(BaseSyncStampDao.Properties.LocalKey.eq(key1))
                .where(BaseSyncStampDao.Properties.RemoteKey.eq(key2))
                .where(BaseSyncStampDao.Properties.Type.eq(type))
                .unique();

        if (baseSyncStamp == null) {
            baseSyncStamp = baseSyncStampDao.queryBuilder()
                    .where(BaseSyncStampDao.Properties.LocalKey.eq(key2))
                    .where(BaseSyncStampDao.Properties.RemoteKey.eq(key1))
                    .where(BaseSyncStampDao.Properties.Type.eq(type))
                    .unique();
        }
        return baseSyncStamp;
    }
}
