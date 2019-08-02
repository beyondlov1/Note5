package com.beyond.note5.sync.datasource.impl;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Document;
import com.beyond.note5.event.AbstractEvent;
import com.beyond.note5.model.dao.SyncInfoDao;
import com.beyond.note5.presenter.DocumentPresenter;
import com.beyond.note5.sync.SyncContext;
import com.beyond.note5.sync.SyncContextAware;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.SqlDataSource;
import com.beyond.note5.sync.model.SqlLogModel;
import com.beyond.note5.sync.model.entity.SyncInfo;
import com.beyond.note5.sync.model.entity.SyncLogInfo;
import com.beyond.note5.sync.model.entity.TraceInfo;
import com.beyond.note5.sync.model.impl.SqlLogModelImpl;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.PreferenceUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class DocumentSqlDataSource<T extends Document> implements SqlDataSource<T>, SyncContextAware {

    private DocumentPresenter<T> documentPresenter;

    private SqlLogModel sqlLogModel;

    private SyncContext context;

    public DocumentSqlDataSource() {
        this.documentPresenter = getDocumentPresenter();
        this.sqlLogModel = new SqlLogModelImpl(MyApplication.getInstance().getDaoSession().getSyncLogInfoDao(),
                clazz().getSimpleName().toLowerCase());
    }

    public DocumentSqlDataSource(DocumentPresenter<T> documentPresenter) {
        this.documentPresenter = documentPresenter;
        this.sqlLogModel = new SqlLogModelImpl(MyApplication.getInstance().getDaoSession().getSyncLogInfoDao(),
                clazz().getSimpleName().toLowerCase());
    }

    protected abstract DocumentPresenter<T> getDocumentPresenter();

    @Override
    public String getKey() {
        return PreferenceUtil.getString(MyApplication.VIRTUAL_USER_ID);
    }

    @Override
    public void add(T t) {
        documentPresenter.add(t);
    }

    @Override
    public void delete(T t) {
        documentPresenter.delete(t);
    }

    @Override
    public void update(T t) {
        T oldNote = documentPresenter.selectById(t.getId());
        documentPresenter.update(t);
        if (!oldNote.getValid() && t.getValid()) {
            EventBus.getDefault().post(getAddSuccessEvent(t));
        } else if (oldNote.getValid() && !t.getValid()) {
            EventBus.getDefault().post(getDeleteSuccessEvent(t));
        }
    }

    protected abstract AbstractEvent<T> getAddSuccessEvent(T t);

    protected abstract AbstractEvent<T> getDeleteSuccessEvent(T t);

    @Override
    public T select(T t) {
        throw new RuntimeException("暂不支持");
    }

    @Override
    public T selectById(String id) throws IOException {
        return documentPresenter.selectById(id);
    }

    @Override
    public List<T> selectByIds(List<String> ids) {
        return documentPresenter.selectByIds(ids);
    }

    @Override
    public List<T> selectAll() throws IOException {
        return documentPresenter.selectAllInAll();
    }

    @Override
    public TraceInfo getLatestTraceInfo() throws IOException {
        List<T> list = selectAll();
        if (list != null && !list.isEmpty()) {
            Collections.sort(list, new Comparator<T>() {
                @Override
                public int compare(T o1, T o2) {
                    return (int) (o1.getLastModifyTime().getTime() - o2.getLastModifyTime().getTime());
                }
            });
            T latestNote = list.get(list.size() - 1);
            return TraceInfo.create(latestNote.getLastModifyTime(), latestNote.getLastModifyTime());
        } else {
            return TraceInfo.ZERO;
        }
    }

    @Override
    public void setLatestTraceInfo(TraceInfo traceInfo) throws IOException {
        //do nothing
    }

    @Override
    public void cover(List<T> all) throws IOException {
        throw new RuntimeException("暂不支持");
    }

    @Override
    public List<T> getChangedData(TraceInfo traceInfo) throws IOException {

        // 日志的操作时间大于上次同步成功的最后修改时间
        // 会改变lastModifyTime类型的, 如 add, update
        List<SyncLogInfo> lastModifyTimeChangeableLogs = sqlLogModel.getAllWhereOperationTimeAfter(
                traceInfo.getLastModifyTime() == null ? new Date(0) : traceInfo.getLastModifyTime());

        // 日志的操作时间大于上次同步成功的同步开始时间 , 并且source是不是对方dataSource的
        // 改变priority这种不更改lastModifyTime的, 如 改变priority
        List<SyncLogInfo> lastModifyTimeUnchangeableLogs = sqlLogModel.getAllWithoutSourceWhereCreateTimeAfter(
                traceInfo.getLastSyncTimeStart() == null ? new Date(0) : traceInfo.getLastSyncTimeStart(),
                context.getCorrespondKey(this));
        List<SyncLogInfo> modifiedLogs = new ArrayList<>();
        modifiedLogs.addAll(lastModifyTimeChangeableLogs);
        modifiedLogs.addAll(lastModifyTimeUnchangeableLogs);
        if (modifiedLogs.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> ids = new HashSet<>(modifiedLogs.size());
        for (SyncLogInfo modifiedLog : modifiedLogs) {
            ids.add(modifiedLog.getDocumentId());
        }

        return selectByIds(new ArrayList<>(ids));
    }

    @Override
    public void save(T t) throws IOException {
        T localNote = documentPresenter.selectById(t.getId());
        if (localNote != null) {
            if (t.getLastModifyTime().after(localNote.getLastModifyTime())
                    || t.getVersion() > localNote.getVersion()) {
                update(t);
            }
        } else {
            add(t);
        }
    }

    @Override
    public void saveAll(List<T> ts) throws IOException {
        saveAll(ts, getKey());
    }

    @Override
    public void saveAll(List<T> ts, String source) throws IOException {
        Map<String, T> map = new HashMap<>(ts.size());
        for (T t : ts) {
            map.put(t.getId(), t);
        }
        List<T> tList = documentPresenter.selectByIds(map.keySet());
        Map<String, T> localMap = new HashMap<>(tList.size());

        for (T localNote : tList) {
            localMap.put(localNote.getId(), localNote);
        }

        List<T> addList = new ArrayList<>();
        List<T> updateList = new ArrayList<>();
        for (String id : map.keySet()) {
            if (localMap.containsKey(id)) {
                if (map.get(id).getLastModifyTime().after(localMap.get(id).getLastModifyTime())
                        || (map.get(id).getVersion() == null ? 0 : map.get(id).getVersion())
                        > (localMap.get(id).getVersion() == null ? 0 : localMap.get(id).getVersion())) {
                    updateList.add(map.get(id));
                }
            } else {
                addList.add(map.get(id));
            }
        }
        documentPresenter.addAllForSync(addList, source);
        documentPresenter.updateAllForSync(updateList, source);
    }

    @Override
    public boolean isChanged(DataSource<T> targetDataSource) throws IOException {
        return !getChangedData(getCorrespondTraceInfo(targetDataSource)).isEmpty();
    }

    @Override
    public TraceInfo getCorrespondTraceInfo(DataSource<T> targetDataSource) throws IOException {
        SyncInfo syncInfo = MyApplication.getInstance().getDaoSession().getSyncInfoDao().queryBuilder()
                .where(SyncInfoDao.Properties.RemoteKey.eq(targetDataSource.getKey()))
                .where(SyncInfoDao.Properties.Type.eq(targetDataSource.clazz().getSimpleName().toLowerCase()))
                .unique();
        return syncInfo == null ? TraceInfo.ZERO : TraceInfo.create(syncInfo.getLastModifyTime(), syncInfo.getLastSyncTimeStart(), syncInfo.getLastSyncTime());
    }

    @Override
    public void setCorrespondTraceInfo(TraceInfo traceInfo, DataSource<T> targetDataSource) throws IOException {
        SyncInfoDao syncInfoDao = MyApplication.getInstance().getDaoSession().getSyncInfoDao();
        SyncInfo syncInfo = syncInfoDao.queryBuilder()
                .where(SyncInfoDao.Properties.RemoteKey.eq(targetDataSource.getKey()))
                .where(SyncInfoDao.Properties.Type.eq(targetDataSource.clazz().getSimpleName().toLowerCase()))
                .unique();
        if (syncInfo == null) {
            SyncInfo info = new SyncInfo();
            info.setId(IDUtil.uuid());
            info.setLocalKey(getKey());
            info.setRemoteKey(targetDataSource.getKey());
            info.setLastModifyTime(traceInfo.getLastModifyTime());
            info.setLastSyncTime(traceInfo.getLastSyncTimeEnd());
            info.setLastSyncTimeStart(traceInfo.getLastSyncTimeStart());
            info.setType(targetDataSource.clazz().getSimpleName().toLowerCase());
            syncInfoDao.insert(info);
        } else {
            syncInfo.setLastModifyTime(traceInfo.getLastModifyTime());
            syncInfo.setLastSyncTime(traceInfo.getLastSyncTimeEnd());
            syncInfo.setLastSyncTimeStart(traceInfo.getLastSyncTimeStart());
            syncInfoDao.update(syncInfo);
        }
    }

    @Override
    public boolean tryLock() {
        return true;
    }

    @Override
    public boolean tryLock(Long time) {
        return true;
    }

    @Override
    public boolean isLocked() {
        return false;
    }

    @Override
    public boolean release() {
        return true;
    }

    @Override
    public void setContext(SyncContext context) {
        this.context = context;
    }

}
