package com.beyond.note5.sync.model.impl;

import com.beyond.note5.sync.model.bean.SyncInfo;
import com.beyond.note5.model.dao.SyncInfoDao;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.model.LSTModel;
import com.beyond.note5.utils.IDUtil;

import java.io.IOException;
import java.util.Date;

public class SqlLSTModel implements LSTModel {


    private DataSource localDataSource;

    private DataSource remoteDataSource;

    private SyncInfoDao syncInfoDao;

    public SqlLSTModel(DataSource localDataSource, DataSource remoteDataSource, SyncInfoDao syncInfoDao) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
        this.syncInfoDao = syncInfoDao;
    }

    @Override
    public Date getLastSyncTime() throws IOException {
        SyncInfo syncInfo = syncInfoDao.queryBuilder()
                .where(SyncInfoDao.Properties.RemoteKey.eq(remoteDataSource.getKey()))
                .unique();
        return syncInfo == null?new Date(0):syncInfo.getLastSyncTime();
    }

    @Override
    public void setLastSyncTime(Date date) throws IOException {
        SyncInfo syncInfo = syncInfoDao.queryBuilder()
                .where(SyncInfoDao.Properties.RemoteKey.eq(remoteDataSource.getKey()))
                .unique();
        if (syncInfo == null){
            SyncInfo info = new SyncInfo();
            info.setId(IDUtil.uuid());
            info.setLocalKey(localDataSource.getKey());
            info.setRemoteKey(remoteDataSource.getKey());
            info.setLastSyncTime(date);
            syncInfoDao.insert(info);
        }else {
            syncInfo.setLastSyncTime(date);
            syncInfoDao.update(syncInfo);
        }
    }
}
