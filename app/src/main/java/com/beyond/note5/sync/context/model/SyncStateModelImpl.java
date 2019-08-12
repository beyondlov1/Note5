package com.beyond.note5.sync.context.model;

import com.beyond.note5.MyApplication;
import com.beyond.note5.model.dao.SyncStateDao;
import com.beyond.note5.sync.context.entity.SyncState;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

public class SyncStateModelImpl implements SyncStateModel {

    private SyncStateDao dao;

    public SyncStateModelImpl() {
        this.dao = MyApplication.getInstance().getDaoSession().getSyncStateDao();
    }

    @Override
    public void add(SyncState syncState) {
        dao.insert(syncState);
    }

    @Override
    public void update(SyncState syncState) {
        dao.update(syncState);
    }

    @Override
    public void save(SyncState syncState) {
        SyncState foundState = dao.queryBuilder()
                .where(SyncStateDao.Properties.DocumentId.eq(syncState.getDocumentId()))
                .unique();

        if (foundState != null) {
            syncState.setId(foundState.getId());
            update(syncState);
        } else {
            add(syncState);
        }

    }

    @Override
    public void saveAll(List<SyncState> successSyncStates) {
        List<String> documentIds = new ArrayList<>(successSyncStates.size());
        for (SyncState successSyncState : successSyncStates) {
            documentIds.add(successSyncState.getDocumentId());
        }
        List<SyncState> list = dao.queryBuilder()
                .where(SyncStateDao.Properties.DocumentId.in(documentIds))
                .list();

        List<SyncState> addList = new ArrayList<>();
        List<SyncState> updateList = new ArrayList<>();
        for (SyncState successSyncState : successSyncStates) {
            boolean found = false;
            for (SyncState foundStateInfo : list) {
                if (StringUtils.equals(successSyncState.getDocumentId(), foundStateInfo.getDocumentId())) {
                    successSyncState.setId(foundStateInfo.getId());
                    updateList.add(successSyncState);
                    found = true;
                }
            }
            if (!found) {
                addList.add(successSyncState);
            }
        }

        dao.insertInTx(addList);
        dao.updateInTx(updateList);
    }

    @Override
    public List<SyncState> findAll(SyncState syncState) {
        QueryBuilder<SyncState> queryBuilder = dao.queryBuilder();
        if (syncState == null) {
            return queryBuilder.list();
        }
        if (syncState.getId() != null) {
            queryBuilder.where(SyncStateDao.Properties.Id.eq(syncState.getId()));
        }

        if (syncState.getDocumentId() != null) {
            queryBuilder.where(SyncStateDao.Properties.DocumentId.eq(syncState.getDocumentId()));
        }

        if (syncState.getLocal() != null) {
            queryBuilder.where(SyncStateDao.Properties.Local.eq(syncState.getLocal()));
        }
        if (syncState.getServer() != null) {
            queryBuilder.where(SyncStateDao.Properties.Server.eq(syncState.getServer()));
        }

        if (syncState.getState() != null) {
            queryBuilder.where(SyncStateDao.Properties.State.eq(syncState.getState()));
        }

        if (syncState.getType() != null) {
            queryBuilder.where(SyncStateDao.Properties.Type.eq(syncState.getType()));
        }
        return queryBuilder.list();
    }

    @Override
    public void deleteAll(SyncState queryState) {
        QueryBuilder<SyncState> builder = dao.queryBuilder();
        if (queryState.getLocal() != null) {
            builder.where(SyncStateDao.Properties.Local.eq(queryState.getLocal()));
        }
        if (queryState.getServer() != null) {
            builder.where(SyncStateDao.Properties.Server.eq(queryState.getServer()));
        }
        if (queryState.getType() != null) {
            builder.where(SyncStateDao.Properties.Type.eq(queryState.getType()));
        }
        List<SyncState> list = builder.list();
        dao.deleteInTx(list);
    }

    @Override
    public void deleteConnectedEachOtherIn(List<String> keys, Class clazz) {
        QueryBuilder<SyncState> builder = dao.queryBuilder();
        builder.where(SyncStateDao.Properties.Local.in(keys));
        builder.where(SyncStateDao.Properties.Server.in(keys));
        builder.where(SyncStateDao.Properties.Type.eq(clazz.getSimpleName().toLowerCase()));
        List<SyncState> list = builder.list();
        dao.deleteInTx(list);
    }
}
