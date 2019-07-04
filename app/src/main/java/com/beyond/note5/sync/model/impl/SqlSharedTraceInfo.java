package com.beyond.note5.sync.model.impl;

import com.beyond.note5.model.dao.SyncInfoDao;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.model.SharedSource;
import com.beyond.note5.sync.model.bean.SyncInfo;
import com.beyond.note5.sync.model.bean.TraceInfo;
import com.beyond.note5.utils.IDUtil;

import java.io.IOException;

public class SqlSharedTraceInfo implements SharedSource<TraceInfo> {

    private DataSource localDataSource;

    private DataSource remoteDataSource;

    private SyncInfoDao syncInfoDao;

    public SqlSharedTraceInfo(DataSource localDataSource, DataSource remoteDataSource, SyncInfoDao syncInfoDao) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
        this.syncInfoDao = syncInfoDao;
    }

    @Override
    public TraceInfo get() throws IOException {
        SyncInfo syncInfo = syncInfoDao.queryBuilder()
                .where(SyncInfoDao.Properties.RemoteKey.eq(remoteDataSource.getKey()))
                .where(SyncInfoDao.Properties.Type.eq(remoteDataSource.clazz().getSimpleName().toLowerCase()))
                .unique();
        return syncInfo == null?TraceInfo.ZERO:TraceInfo.create(syncInfo.getLastModifyTime(),syncInfo.getLastSyncTime());
    }

    @Override
    public void set(TraceInfo traceInfo) throws IOException {
        SyncInfo syncInfo = syncInfoDao.queryBuilder()
                .where(SyncInfoDao.Properties.RemoteKey.eq(remoteDataSource.getKey()))
                .where(SyncInfoDao.Properties.Type.eq(remoteDataSource.clazz().getSimpleName().toLowerCase()))
                .unique();
        if (syncInfo == null){
            SyncInfo info = new SyncInfo();
            info.setId(IDUtil.uuid());
            info.setLocalKey(localDataSource.getKey());
            info.setRemoteKey(remoteDataSource.getKey());
            info.setLastModifyTime(traceInfo.getLastModifyTime());
            info.setLastSyncTime(traceInfo.getLastSyncTimeEnd());
            info.setType(remoteDataSource.clazz().getSimpleName().toLowerCase());
            syncInfoDao.insert(info);
        }else {
            syncInfo.setLastModifyTime(traceInfo.getLastModifyTime());
            syncInfo.setLastSyncTime(traceInfo.getLastSyncTimeEnd());
            syncInfoDao.update(syncInfo);
        }
    }
}
