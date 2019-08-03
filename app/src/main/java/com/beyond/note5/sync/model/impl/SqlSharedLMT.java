package com.beyond.note5.sync.model.impl;

import com.beyond.note5.model.dao.LatestSyncStampDao;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.entity.LatestSyncStamp;
import com.beyond.note5.sync.model.LMTSharedSource;
import com.beyond.note5.utils.IDUtil;

import java.io.IOException;
import java.util.Date;

@Deprecated
public class SqlSharedLMT implements LMTSharedSource {


    private DataSource localDataSource;

    private DataSource remoteDataSource;

    private LatestSyncStampDao latestSyncStampDao;

    public SqlSharedLMT(DataSource localDataSource, DataSource remoteDataSource, LatestSyncStampDao latestSyncStampDao) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
        this.latestSyncStampDao = latestSyncStampDao;
    }

    @Override
    public Date get() throws IOException {
        LatestSyncStamp latestSyncStamp = latestSyncStampDao.queryBuilder()
                .where(LatestSyncStampDao.Properties.RemoteKey.eq(remoteDataSource.getKey()))
                .where(LatestSyncStampDao.Properties.Type.eq(remoteDataSource.clazz().getSimpleName().toLowerCase()))
                .unique();
        return latestSyncStamp == null?new Date(0): latestSyncStamp.getLastSyncTime();
    }

    @Override
    public void set(Date date) throws IOException {
        LatestSyncStamp latestSyncStamp = latestSyncStampDao.queryBuilder()
                .where(LatestSyncStampDao.Properties.RemoteKey.eq(remoteDataSource.getKey()))
                .where(LatestSyncStampDao.Properties.Type.eq(remoteDataSource.clazz().getSimpleName().toLowerCase()))
                .unique();
        if (latestSyncStamp == null){
            LatestSyncStamp info = new LatestSyncStamp();
            info.setId(IDUtil.uuid());
            info.setLocalKey(localDataSource.getKey());
            info.setRemoteKey(remoteDataSource.getKey());
            info.setLastModifyTime(date);
            info.setType(remoteDataSource.clazz().getSimpleName().toLowerCase());
            latestSyncStampDao.insert(info);
        }else {
            latestSyncStamp.setLastModifyTime(date);
            latestSyncStampDao.update(latestSyncStamp);
        }
    }
}
