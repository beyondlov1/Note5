package com.beyond.note5.sync.model.impl;

import com.beyond.note5.MyApplication;
import com.beyond.note5.model.dao.SyncStateInfoDao;
import com.beyond.note5.sync.model.SyncStateModel;
import com.beyond.note5.sync.model.bean.SyncStateInfo;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

public class SyncStateModelImpl implements SyncStateModel {

    private SyncStateInfoDao dao;

    public SyncStateModelImpl() {
        this.dao = MyApplication.getInstance().getDaoSession().getSyncStateInfoDao();
    }

    @Override
    public void add(SyncStateInfo syncStateInfo) {
        dao.insert(syncStateInfo);
    }

    @Override
    public void update(SyncStateInfo syncStateInfo) {
        dao.update(syncStateInfo);
    }

    @Override
    public void saveAll(ArrayList<SyncStateInfo> successSyncStates) {
        List<SyncStateInfo> list = dao.queryBuilder()
                .where(SyncStateInfoDao.Properties.Id.in(successSyncStates))
                .list();

        List<SyncStateInfo> addList = new ArrayList<>();
        List<SyncStateInfo> updateList = new ArrayList<>();
        for (SyncStateInfo successSyncState : successSyncStates) {
            boolean found = false;
            for (SyncStateInfo syncStateInfo : list) {
                if (StringUtils.equals(successSyncState.getDocumentId(),syncStateInfo.getDocumentId())){
                    updateList.add(successSyncState);
                    found = true;
                }
            }
            if (!found){
                addList.add(successSyncState);
            }
        }

        dao.insertInTx(addList);
        dao.updateInTx(updateList);
    }

    @Override
    public List<SyncStateInfo> select(SyncStateInfo syncStateInfo) {
        QueryBuilder<SyncStateInfo> queryBuilder = dao.queryBuilder();
        if (syncStateInfo.getId() != null) {
            queryBuilder.where(SyncStateInfoDao.Properties.Id.eq(syncStateInfo.getId()));
        }

        if (syncStateInfo.getDocumentId() != null) {
            queryBuilder.where(SyncStateInfoDao.Properties.DocumentId.eq(syncStateInfo.getDocumentId()));
        }

        if (syncStateInfo.getLocal() != null) {
            queryBuilder.where(SyncStateInfoDao.Properties.Local.eq(syncStateInfo.getLocal()));
        }
        if (syncStateInfo.getServer() != null) {
            queryBuilder.where(SyncStateInfoDao.Properties.Server.eq(syncStateInfo.getServer()));
        }

        if (syncStateInfo.getState() != null) {
            queryBuilder.where(SyncStateInfoDao.Properties.State.eq(syncStateInfo.getState()));
        }

        if (syncStateInfo.getType() != null) {
            queryBuilder.where(SyncStateInfoDao.Properties.Type.eq(syncStateInfo.getType()));
        }
        return queryBuilder.list();
    }

    @Override
    public void deleteAll(SyncStateInfo queryState) {
        List<SyncStateInfo> list = dao.queryBuilder()
                .where(SyncStateInfoDao.Properties.Local.eq(queryState.getLocal()))
                .where(SyncStateInfoDao.Properties.Server.eq(queryState.getServer()))
                .where(SyncStateInfoDao.Properties.Type.eq(queryState.getType()))
                .list();
        dao.deleteInTx(list);
    }
}
