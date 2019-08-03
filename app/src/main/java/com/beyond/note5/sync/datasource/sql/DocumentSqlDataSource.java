package com.beyond.note5.sync.datasource.sql;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Document;
import com.beyond.note5.presenter.DocumentPresenter;
import com.beyond.note5.sync.context.SyncContext;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.SyncStampModel;
import com.beyond.note5.sync.datasource.entity.SyncStamp;
import com.beyond.note5.sync.datasource.sql.model.SqlBaseSyncStampModel;
import com.beyond.note5.sync.datasource.sql.model.SqlLogModel;
import com.beyond.note5.sync.datasource.sql.model.SqlLogModelImpl;
import com.beyond.note5.sync.exception.SaveException;
import com.beyond.note5.sync.model.entity.TraceLog;
import com.beyond.note5.utils.PreferenceUtil;

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

public abstract class DocumentSqlDataSource<T extends Document> implements SqlDataSource<T> {

    protected SyncContext context;

    private DocumentPresenter<T> documentPresenter;

    private SqlLogModel sqlLogModel;

    private SyncStampModel baseSyncStampModel;

    private String oppositeKey;

    public DocumentSqlDataSource(String oppositeKey) {
        this.oppositeKey = oppositeKey;
        this.documentPresenter = getDocumentPresenter();
        this.sqlLogModel = new SqlLogModelImpl(
                MyApplication.getInstance().getDaoSession().getTraceLogDao(),
                clazz().getSimpleName().toLowerCase());
        this.baseSyncStampModel = new SqlBaseSyncStampModel(
                MyApplication.getInstance().getDaoSession().getLatestSyncStampDao(),
                getKey(),
                oppositeKey,
                clazz().getSimpleName().toLowerCase()
        );
    }

    protected abstract DocumentPresenter<T> getDocumentPresenter();

    @Override
    public String getKey() {
        return PreferenceUtil.getString(MyApplication.VIRTUAL_USER_ID);
    }

    @Override
    public void saveAll(List<T> ts) throws IOException, SaveException {
        saveAll(ts, oppositeKey);
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
    public List<T> selectAll() throws IOException {
        return documentPresenter.selectAllInAll();
    }


    @Override
    public boolean isChanged(DataSource<T> targetDataSource) throws IOException {
        return !getChangedData(getLastSyncStamp(targetDataSource)).isEmpty();
    }

    @Override
    public List<T> getChangedData(SyncStamp syncStamp) throws IOException {

        // 日志的操作时间大于上次同步成功的最后修改时间
        // 会改变lastModifyTime类型的, 如 add, update
        List<TraceLog> lastModifyTimeChangeableLogs = sqlLogModel.getAllWhereOperationTimeAfter(
                syncStamp.getLastModifyTime() == null ? new Date(0) : syncStamp.getLastModifyTime());

        // 日志的操作时间大于上次同步成功的同步开始时间 , 并且source是不是对方dataSource的
        // 改变priority这种不更改lastModifyTime的, 如 改变priority
        List<TraceLog> lastModifyTimeUnchangeableLogs = sqlLogModel.getAllWithoutSourceWhereCreateTimeAfter(
                syncStamp.getLastSyncTimeStart() == null ? new Date(0) : syncStamp.getLastSyncTimeStart(),
                oppositeKey);
        List<TraceLog> modifiedLogs = new ArrayList<>();
        modifiedLogs.addAll(lastModifyTimeChangeableLogs);
        modifiedLogs.addAll(lastModifyTimeUnchangeableLogs);
        if (modifiedLogs.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> ids = new HashSet<>(modifiedLogs.size());
        for (TraceLog modifiedLog : modifiedLogs) {
            ids.add(modifiedLog.getDocumentId());
        }

        return selectByIds(new ArrayList<>(ids));
    }

    private List<T> selectByIds(List<String> ids) {
        return documentPresenter.selectByIds(ids);
    }

    @Override
    public SyncStamp getLastSyncStamp(DataSource<T> targetDataSource) throws IOException {
        return baseSyncStampModel.retrieve();
    }

    @Override
    public void updateLastSyncStamp(SyncStamp syncStamp, DataSource<T> targetDataSource) throws IOException {
        baseSyncStampModel.update(syncStamp);
    }

    @Override
    public SyncStamp getLatestSyncStamp() throws IOException {
        List<T> list = selectAll();
        if (list != null && !list.isEmpty()) {
            Collections.sort(list, new Comparator<T>() {
                @Override
                public int compare(T o1, T o2) {
                    return (int) (o1.getLastModifyTime().getTime() - o2.getLastModifyTime().getTime());
                }
            });
            T latestNote = list.get(list.size() - 1);
            return SyncStamp.create(latestNote.getLastModifyTime(), latestNote.getLastModifyTime());
        } else {
            return SyncStamp.ZERO;
        }
    }

    @Override
    public void updateLatestSyncStamp(SyncStamp syncStamp) throws IOException {
        //do nothing
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
