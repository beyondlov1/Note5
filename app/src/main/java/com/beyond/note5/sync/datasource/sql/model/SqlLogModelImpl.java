package com.beyond.note5.sync.datasource.sql.model;

import com.beyond.note5.model.dao.TraceLogDao;
import com.beyond.note5.sync.model.entity.TraceLog;
import com.beyond.note5.utils.PreferenceUtil;

import org.apache.commons.collections4.ListUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.beyond.note5.MyApplication.VIRTUAL_USER_ID;

public class SqlLogModelImpl<T> implements SqlLogModel {

    private TraceLogDao traceLogDao;

    private String type;

    public SqlLogModelImpl(TraceLogDao traceLogDao, String type) {
        this.traceLogDao = traceLogDao;
        this.type = type;
    }

    @Override
    public void saveAll(List<TraceLog> traceLogs) throws IOException {
        List<TraceLog> addInfos = new ArrayList<>();
        List<TraceLog> updateInfos = new ArrayList<>();

        List<TraceLog> localLogInfos = getAll();
        List<TraceLog> modified = ListUtils.subtract(traceLogs, localLogInfos);
        for (TraceLog traceLog : modified) {
            if (localLogInfos.contains(traceLog)){
                updateInfos.add(traceLog);
            }else {
                addInfos.add(traceLog);
            }
        }

        traceLogDao.insertInTx(addInfos);
        traceLogDao.updateInTx(updateInfos);
    }

    @Override
    public List<TraceLog> getAll() throws IOException {
        return traceLogDao.loadAll();
    }

    @Override
    public List<TraceLog> getLocalAdded(Date lastSyncTime) {
        return traceLogDao.queryBuilder()
                .where(TraceLogDao.Properties.Source.eq(PreferenceUtil.getString(VIRTUAL_USER_ID)))
                .where(TraceLogDao.Properties.Operation.eq(TraceLog.ADD))
                .where(TraceLogDao.Properties.OperationTime.gt(lastSyncTime))
                .where(TraceLogDao.Properties.Type.eq(type))
                .list();
    }

    @Override
    public List<TraceLog> getLocalUpdated(Date lastSyncTime) {
        return traceLogDao.queryBuilder()
                .where(TraceLogDao.Properties.Source.eq(PreferenceUtil.getString(VIRTUAL_USER_ID)))
                .where(TraceLogDao.Properties.OperationTime.gt(lastSyncTime))
                .where(TraceLogDao.Properties.Operation.eq(TraceLog.UPDATE))
                .where(TraceLogDao.Properties.Type.eq(type))
                .list();
    }

    @Override
    public List<TraceLog> getRemoteAdded(Date lastSyncTime) {
        return traceLogDao.queryBuilder()
                .where(TraceLogDao.Properties.Source.notEq(PreferenceUtil.getString(VIRTUAL_USER_ID)))
                .where(TraceLogDao.Properties.Operation.eq(TraceLog.ADD))
                .where(TraceLogDao.Properties.OperationTime.gt(lastSyncTime))
                .where(TraceLogDao.Properties.Type.eq(type))
                .list();
    }

    @Override
    public List<TraceLog> getRemoteUpdated(Date lastSyncTime) {
        return traceLogDao.queryBuilder()
                .where(TraceLogDao.Properties.Source.notEq(PreferenceUtil.getString(VIRTUAL_USER_ID)))
                .where(TraceLogDao.Properties.OperationTime.gt(lastSyncTime))
                .where(TraceLogDao.Properties.Operation.eq(TraceLog.UPDATE))
                .where(TraceLogDao.Properties.Type.eq(type))
                .list();
    }

    @Override
    public Date getLatestLastModifyTime() {
        TraceLog log = traceLogDao.queryBuilder()
                .orderDesc(TraceLogDao.Properties.OperationTime)
                .where(TraceLogDao.Properties.Type.eq(type))
                .limit(1)
                .unique();
        if (log==null){
            return new Date(0);
        }
        return log.getOperationTime();
    }

    @Override
    public List<TraceLog> getAllWhereOperationTimeAfter(Date date) {
        return traceLogDao.queryBuilder()
                .where(TraceLogDao.Properties.OperationTime.gt(date))
                .where(TraceLogDao.Properties.Type.eq(type))
                .list();
    }

    @Override
    public List<TraceLog> getAllWhereCreateTimeAfter(Date date) {
        return traceLogDao.queryBuilder()
                .where(TraceLogDao.Properties.CreateTime.gt(date))
                .where(TraceLogDao.Properties.Type.eq(type))
                .list();
    }

    @Override
    public List<TraceLog> getAllBySourceWhereCreateTimeAfter(Date date, String source) {
        return traceLogDao.queryBuilder()
                .where(TraceLogDao.Properties.CreateTime.gt(date))
                .where(TraceLogDao.Properties.Source.eq(source))
                .where(TraceLogDao.Properties.Type.eq(type))
                .list();
    }

    @Override
    public List<TraceLog> getAllBySourceWhereOperationTimeAfter(Date date, String source) {
        return traceLogDao.queryBuilder()
                .where(TraceLogDao.Properties.OperationTime.gt(date))
                .where(TraceLogDao.Properties.Source.eq(source))
                .where(TraceLogDao.Properties.Type.eq(type))
                .list();
    }

    @Override
    public List<TraceLog> getAllWithoutSourceWhereCreateTimeAfter(Date date, String excludeSource) {
        return traceLogDao.queryBuilder()
                .where(TraceLogDao.Properties.CreateTime.gt(date))
                .where(TraceLogDao.Properties.Source.notEq(excludeSource))
                .where(TraceLogDao.Properties.Type.eq(type))
                .list();
    }

}
