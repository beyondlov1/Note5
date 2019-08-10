package com.beyond.note5.sync.model.impl;

import com.beyond.note5.model.dao.BaseSyncStampDao;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.entity.BaseSyncStamp;
import com.beyond.note5.sync.datasource.entity.SyncStamp;
import com.beyond.note5.sync.model.SharedSource;
import com.beyond.note5.utils.IDUtil;

import java.io.IOException;

public class SqlSharedTraceInfo implements SharedSource<SyncStamp> {

    private DataSource localDataSource;

    private DataSource remoteDataSource;

    private BaseSyncStampDao syncInfoDao;

    public SqlSharedTraceInfo(DataSource localDataSource, DataSource remoteDataSource, BaseSyncStampDao syncInfoDao) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
        this.syncInfoDao = syncInfoDao;
    }

    @Override
    public SyncStamp get() throws IOException {
        BaseSyncStamp baseSyncStamp = syncInfoDao.queryBuilder()
                .where(BaseSyncStampDao.Properties.RemoteKey.eq(remoteDataSource.getKey()))
                .where(BaseSyncStampDao.Properties.Type.eq(remoteDataSource.clazz().getSimpleName().toLowerCase()))
                .unique();
        return baseSyncStamp == null? SyncStamp.ZERO: SyncStamp.create(baseSyncStamp.getLastModifyTime(), baseSyncStamp.getLastSyncTime());
    }

    @Override
    public void set(SyncStamp syncStamp) throws IOException {
        BaseSyncStamp baseSyncStamp = syncInfoDao.queryBuilder()
                .where(BaseSyncStampDao.Properties.RemoteKey.eq(remoteDataSource.getKey()))
                .where(BaseSyncStampDao.Properties.Type.eq(remoteDataSource.clazz().getSimpleName().toLowerCase()))
                .unique();
        if (baseSyncStamp == null){
            BaseSyncStamp info = new BaseSyncStamp();
            info.setId(IDUtil.uuid());
            info.setLocalKey(localDataSource.getKey());
            info.setRemoteKey(remoteDataSource.getKey());
            info.setLastModifyTime(syncStamp.getLastModifyTime());
            info.setLastSyncTime(syncStamp.getLastSyncTimeEnd());
            info.setType(remoteDataSource.clazz().getSimpleName().toLowerCase());
            syncInfoDao.insert(info);
        }else {
            baseSyncStamp.setLastModifyTime(syncStamp.getLastModifyTime());
            baseSyncStamp.setLastSyncTime(syncStamp.getLastSyncTimeEnd());
            syncInfoDao.update(baseSyncStamp);
        }
    }
}
