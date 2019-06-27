package com.beyond.note5.sync.model.impl;

import com.beyond.note5.sync.model.bean.SyncInfo;
import com.beyond.note5.model.dao.SyncInfoDao;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.model.LMTSharedSource;
import com.beyond.note5.utils.IDUtil;

import java.io.IOException;
import java.util.Date;

@Deprecated
public class SqlSharedLMT implements LMTSharedSource {


    private DataSource localDataSource;

    private DataSource remoteDataSource;

    private SyncInfoDao syncInfoDao;

    public SqlSharedLMT(DataSource localDataSource, DataSource remoteDataSource, SyncInfoDao syncInfoDao) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
        this.syncInfoDao = syncInfoDao;
    }

    @Override
    public Date get() throws IOException {
        SyncInfo syncInfo = syncInfoDao.queryBuilder()
                .where(SyncInfoDao.Properties.RemoteKey.eq(remoteDataSource.getKey()))
                .where(SyncInfoDao.Properties.Type.eq(remoteDataSource.clazz().getSimpleName().toLowerCase()))
                .unique();
        return syncInfo == null?new Date(0):syncInfo.getLastSyncTime();
    }

    @Override
    public void set(Date date) throws IOException {
        SyncInfo syncInfo = syncInfoDao.queryBuilder()
                .where(SyncInfoDao.Properties.RemoteKey.eq(remoteDataSource.getKey()))
                .where(SyncInfoDao.Properties.Type.eq(remoteDataSource.clazz().getSimpleName().toLowerCase()))
                .unique();
        if (syncInfo == null){
            SyncInfo info = new SyncInfo();
            info.setId(IDUtil.uuid());
            info.setLocalKey(localDataSource.getKey());
            info.setRemoteKey(remoteDataSource.getKey());
            info.setLastModifyTime(date);
            info.setType(remoteDataSource.clazz().getSimpleName().toLowerCase());
            syncInfoDao.insert(info);
        }else {
            syncInfo.setLastModifyTime(date);
            syncInfoDao.update(syncInfo);
        }
    }
}
