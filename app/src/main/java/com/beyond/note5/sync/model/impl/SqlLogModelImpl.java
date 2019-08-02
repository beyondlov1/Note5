package com.beyond.note5.sync.model.impl;

import com.beyond.note5.model.dao.SyncLogInfoDao;
import com.beyond.note5.sync.model.SqlLogModel;
import com.beyond.note5.sync.model.entity.SyncLogInfo;
import com.beyond.note5.utils.PreferenceUtil;

import org.apache.commons.collections4.ListUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.beyond.note5.MyApplication.VIRTUAL_USER_ID;

public class SqlLogModelImpl<T> implements SqlLogModel {

    private SyncLogInfoDao logInfoDao;

    private String type;

    public SqlLogModelImpl(SyncLogInfoDao logInfoDao, String type) {
        this.logInfoDao = logInfoDao;
        this.type = type;
    }

    @Override
    public void saveAll(List<SyncLogInfo> syncLogInfos) throws IOException {
        List<SyncLogInfo> addInfos = new ArrayList<>();
        List<SyncLogInfo> updateInfos = new ArrayList<>();

        List<SyncLogInfo> localLogInfos = getAll();
        List<SyncLogInfo> modified = ListUtils.subtract(syncLogInfos, localLogInfos);
        for (SyncLogInfo syncLogInfo : modified) {
            if (localLogInfos.contains(syncLogInfo)){
                updateInfos.add(syncLogInfo);
            }else {
                addInfos.add(syncLogInfo);
            }
        }

        logInfoDao.insertInTx(addInfos);
        logInfoDao.updateInTx(updateInfos);
    }

    @Override
    public List<SyncLogInfo> getAll() throws IOException {
        return logInfoDao.loadAll();
    }

    @Override
    public List<SyncLogInfo> getLocalAdded(Date lastSyncTime) {
        return logInfoDao.queryBuilder()
                .where(SyncLogInfoDao.Properties.Source.eq(PreferenceUtil.getString(VIRTUAL_USER_ID)))
                .where(SyncLogInfoDao.Properties.Operation.eq(SyncLogInfo.ADD))
                .where(SyncLogInfoDao.Properties.OperationTime.gt(lastSyncTime))
                .where(SyncLogInfoDao.Properties.Type.eq(type))
                .list();
    }

    @Override
    public List<SyncLogInfo> getLocalUpdated(Date lastSyncTime) {
        return logInfoDao.queryBuilder()
                .where(SyncLogInfoDao.Properties.Source.eq(PreferenceUtil.getString(VIRTUAL_USER_ID)))
                .where(SyncLogInfoDao.Properties.OperationTime.gt(lastSyncTime))
                .where(SyncLogInfoDao.Properties.Operation.eq(SyncLogInfo.UPDATE))
                .where(SyncLogInfoDao.Properties.Type.eq(type))
                .list();
    }

    @Override
    public List<SyncLogInfo> getRemoteAdded(Date lastSyncTime) {
        return logInfoDao.queryBuilder()
                .where(SyncLogInfoDao.Properties.Source.notEq(PreferenceUtil.getString(VIRTUAL_USER_ID)))
                .where(SyncLogInfoDao.Properties.Operation.eq(SyncLogInfo.ADD))
                .where(SyncLogInfoDao.Properties.OperationTime.gt(lastSyncTime))
                .where(SyncLogInfoDao.Properties.Type.eq(type))
                .list();
    }

    @Override
    public List<SyncLogInfo> getRemoteUpdated(Date lastSyncTime) {
        return logInfoDao.queryBuilder()
                .where(SyncLogInfoDao.Properties.Source.notEq(PreferenceUtil.getString(VIRTUAL_USER_ID)))
                .where(SyncLogInfoDao.Properties.OperationTime.gt(lastSyncTime))
                .where(SyncLogInfoDao.Properties.Operation.eq(SyncLogInfo.UPDATE))
                .where(SyncLogInfoDao.Properties.Type.eq(type))
                .list();
    }

    @Override
    public Date getLatestLastModifyTime() {
        SyncLogInfo log = logInfoDao.queryBuilder()
                .orderDesc(SyncLogInfoDao.Properties.OperationTime)
                .where(SyncLogInfoDao.Properties.Type.eq(type))
                .limit(1)
                .unique();
        if (log==null){
            return new Date(0);
        }
        return log.getOperationTime();
    }

    @Override
    public List<SyncLogInfo> getAllWhereOperationTimeAfter(Date date) {
        return logInfoDao.queryBuilder()
                .where(SyncLogInfoDao.Properties.OperationTime.gt(date))
                .where(SyncLogInfoDao.Properties.Type.eq(type))
                .list();
    }

    @Override
    public List<SyncLogInfo> getAllWhereCreateTimeAfter(Date date) {
        return logInfoDao.queryBuilder()
                .where(SyncLogInfoDao.Properties.CreateTime.gt(date))
                .where(SyncLogInfoDao.Properties.Type.eq(type))
                .list();
    }

    @Override
    public List<SyncLogInfo> getAllBySourceWhereCreateTimeAfter(Date date,String source) {
        return logInfoDao.queryBuilder()
                .where(SyncLogInfoDao.Properties.CreateTime.gt(date))
                .where(SyncLogInfoDao.Properties.Source.eq(source))
                .where(SyncLogInfoDao.Properties.Type.eq(type))
                .list();
    }

    @Override
    public List<SyncLogInfo> getAllBySourceWhereOperationTimeAfter(Date date, String source) {
        return logInfoDao.queryBuilder()
                .where(SyncLogInfoDao.Properties.OperationTime.gt(date))
                .where(SyncLogInfoDao.Properties.Source.eq(source))
                .where(SyncLogInfoDao.Properties.Type.eq(type))
                .list();
    }

    @Override
    public List<SyncLogInfo> getAllWithoutSourceWhereCreateTimeAfter(Date date, String excludeSource) {
        return logInfoDao.queryBuilder()
                .where(SyncLogInfoDao.Properties.CreateTime.gt(date))
                .where(SyncLogInfoDao.Properties.Source.notEq(excludeSource))
                .where(SyncLogInfoDao.Properties.Type.eq(type))
                .list();
    }

}
