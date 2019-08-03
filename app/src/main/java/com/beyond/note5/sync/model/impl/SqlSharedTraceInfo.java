package com.beyond.note5.sync.model.impl;

import com.beyond.note5.model.dao.LatestSyncStampDao;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.entity.LatestSyncStamp;
import com.beyond.note5.sync.model.SharedSource;
import com.beyond.note5.sync.datasource.entity.SyncStamp;
import com.beyond.note5.utils.IDUtil;

import java.io.IOException;

public class SqlSharedTraceInfo implements SharedSource<SyncStamp> {

    private DataSource localDataSource;

    private DataSource remoteDataSource;

    private LatestSyncStampDao syncInfoDao;

    public SqlSharedTraceInfo(DataSource localDataSource, DataSource remoteDataSource, LatestSyncStampDao syncInfoDao) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
        this.syncInfoDao = syncInfoDao;
    }

    @Override
    public SyncStamp get() throws IOException {
        LatestSyncStamp latestSyncStamp = syncInfoDao.queryBuilder()
                .where(LatestSyncStampDao.Properties.RemoteKey.eq(remoteDataSource.getKey()))
                .where(LatestSyncStampDao.Properties.Type.eq(remoteDataSource.clazz().getSimpleName().toLowerCase()))
                .unique();
        return latestSyncStamp == null? SyncStamp.ZERO: SyncStamp.create(latestSyncStamp.getLastModifyTime(), latestSyncStamp.getLastSyncTime());
    }

    @Override
    public void set(SyncStamp syncStamp) throws IOException {
        LatestSyncStamp latestSyncStamp = syncInfoDao.queryBuilder()
                .where(LatestSyncStampDao.Properties.RemoteKey.eq(remoteDataSource.getKey()))
                .where(LatestSyncStampDao.Properties.Type.eq(remoteDataSource.clazz().getSimpleName().toLowerCase()))
                .unique();
        if (latestSyncStamp == null){
            LatestSyncStamp info = new LatestSyncStamp();
            info.setId(IDUtil.uuid());
            info.setLocalKey(localDataSource.getKey());
            info.setRemoteKey(remoteDataSource.getKey());
            info.setLastModifyTime(syncStamp.getLastModifyTime());
            info.setLastSyncTime(syncStamp.getLastSyncTimeEnd());
            info.setType(remoteDataSource.clazz().getSimpleName().toLowerCase());
            syncInfoDao.insert(info);
        }else {
            latestSyncStamp.setLastModifyTime(syncStamp.getLastModifyTime());
            latestSyncStamp.setLastSyncTime(syncStamp.getLastSyncTimeEnd());
            syncInfoDao.update(latestSyncStamp);
        }
    }
}
