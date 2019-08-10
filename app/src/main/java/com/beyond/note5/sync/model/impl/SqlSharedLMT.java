package com.beyond.note5.sync.model.impl;

import com.beyond.note5.model.dao.BaseSyncStampDao;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.entity.BaseSyncStamp;
import com.beyond.note5.sync.model.LMTSharedSource;
import com.beyond.note5.utils.IDUtil;

import java.io.IOException;
import java.util.Date;

@Deprecated
public class SqlSharedLMT implements LMTSharedSource {


    private DataSource localDataSource;

    private DataSource remoteDataSource;

    private BaseSyncStampDao baseSyncStampDao;

    public SqlSharedLMT(DataSource localDataSource, DataSource remoteDataSource, BaseSyncStampDao baseSyncStampDao) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
        this.baseSyncStampDao = baseSyncStampDao;
    }

    @Override
    public Date get() throws IOException {
        BaseSyncStamp baseSyncStamp = baseSyncStampDao.queryBuilder()
                .where(BaseSyncStampDao.Properties.RemoteKey.eq(remoteDataSource.getKey()))
                .where(BaseSyncStampDao.Properties.Type.eq(remoteDataSource.clazz().getSimpleName().toLowerCase()))
                .unique();
        return baseSyncStamp == null?new Date(0): baseSyncStamp.getLastSyncTime();
    }

    @Override
    public void set(Date date) throws IOException {
        BaseSyncStamp baseSyncStamp = baseSyncStampDao.queryBuilder()
                .where(BaseSyncStampDao.Properties.RemoteKey.eq(remoteDataSource.getKey()))
                .where(BaseSyncStampDao.Properties.Type.eq(remoteDataSource.clazz().getSimpleName().toLowerCase()))
                .unique();
        if (baseSyncStamp == null){
            BaseSyncStamp info = new BaseSyncStamp();
            info.setId(IDUtil.uuid());
            info.setLocalKey(localDataSource.getKey());
            info.setRemoteKey(remoteDataSource.getKey());
            info.setLastModifyTime(date);
            info.setType(remoteDataSource.clazz().getSimpleName().toLowerCase());
            baseSyncStampDao.insert(info);
        }else {
            baseSyncStamp.setLastModifyTime(date);
            baseSyncStampDao.update(baseSyncStamp);
        }
    }
}
