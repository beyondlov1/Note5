package com.beyond.note5.sync.datasource.impl;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Note;
import com.beyond.note5.event.note.AddNoteSuccessEvent;
import com.beyond.note5.event.note.DeleteNoteSuccessEvent;
import com.beyond.note5.model.dao.SyncInfoDao;
import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.presenter.NotePresenterImpl;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.model.SqlLogModel;
import com.beyond.note5.sync.model.bean.SyncInfo;
import com.beyond.note5.sync.model.bean.SyncLogInfo;
import com.beyond.note5.sync.model.bean.TraceInfo;
import com.beyond.note5.sync.model.impl.SqlLogModelImpl;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.PreferenceUtil;
import com.beyond.note5.view.adapter.view.NoteViewAdapter;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoteSqlDataSource implements DataSource<Note> {

    private NotePresenter notePresenter;

    private SqlLogModel sqlLogModel;

    public NoteSqlDataSource() {
        this.notePresenter = new NotePresenterImpl(new MyNoteView());
        this.sqlLogModel = new SqlLogModelImpl(MyApplication.getInstance().getDaoSession().getSyncLogInfoDao(),
                clazz().getSimpleName().toLowerCase());
    }

    public NoteSqlDataSource(NotePresenter notePresenter) {
        this.notePresenter = notePresenter;
        this.sqlLogModel = new SqlLogModelImpl(MyApplication.getInstance().getDaoSession().getSyncLogInfoDao(),
                clazz().getSimpleName().toLowerCase());
    }

    @Override
    public String getKey() {
        return PreferenceUtil.getString(MyApplication.VIRTUAL_USER_ID);
    }

    @Override
    public void add(Note note) {
        notePresenter.add(note);
    }

    @Override
    public void delete(Note note) {
        notePresenter.delete(note);
    }

    @Override
    public void update(Note note) {
        Note oldNote = notePresenter.selectById(note.getId());
        notePresenter.update(note);
        if (!oldNote.getValid() && note.getValid()) {
            EventBus.getDefault().post(new AddNoteSuccessEvent(note));
        } else if (oldNote.getValid() && !note.getValid()) {
            EventBus.getDefault().post(new DeleteNoteSuccessEvent(note));
        }
    }

    @Override
    public Note select(Note note) {
        throw new RuntimeException("暂不支持");
    }

    @Override
    public Note selectById(String id) throws IOException {
        return notePresenter.selectById(id);
    }

    @Override
    public List<Note> selectByIds(List<String> ids) {
        return notePresenter.selectByIds(ids);
    }

    @Override
    public List<Note> selectAll() throws IOException {
        return notePresenter.selectAllInAll();
    }

    @Override
    public List<Note> selectByModifiedDate(Date date) throws IOException {
        return notePresenter.selectByModifiedDate(date);
    }

    @Override
    public TraceInfo getLatestTraceInfo() throws IOException {
        List<Note> list = selectAll();
        if (list!=null && !list.isEmpty()){
            Collections.sort(list, new Comparator<Note>() {
                @Override
                public int compare(Note o1, Note o2) {
                    return (int) (o1.getLastModifyTime().getTime()-o2.getLastModifyTime().getTime());
                }
            });
            Note latestNote = list.get(list.size()-1);
            return TraceInfo.create(latestNote.getLastModifyTime(),latestNote.getLastModifyTime());
        }else {
            return TraceInfo.ZERO;
        }
    }

    @Override
    public void setLatestTraceInfo(TraceInfo traceInfo) throws IOException {
        //do nothing
    }

    @Override
    public void cover(List<Note> all) throws IOException {
        throw new RuntimeException("暂不支持");
    }

    @Override
    public Class<Note> clazz() {
        return Note.class;
    }

    @Override
    public List<Note> getModifiedData(TraceInfo traceInfo) throws IOException {

        List<SyncLogInfo> modifiedLogs = sqlLogModel.getAllAfter(
                traceInfo.getLastSyncTimeStart() == null?new Date(0):traceInfo.getLastSyncTimeStart());
        if (modifiedLogs.isEmpty()){
            return new ArrayList<>();
        }

        List<String> ids = new ArrayList<>(modifiedLogs.size());
        for (SyncLogInfo modifiedLog : modifiedLogs) {
            ids.add(modifiedLog.getDocumentId());
        }

        return selectByIds(ids);
    }

    @Override
    public void save(Note note) throws IOException {
        Note localNote = notePresenter.selectById(note.getId());
        if (localNote != null) {
            if (note.getLastModifyTime().after(localNote.getLastModifyTime())) {
                update(note);
            }
        } else {
            add(note);
        }
    }

    @Override
    public void saveAll(List<Note> notes) throws IOException {
        Map<String, Note> map = new HashMap<>(notes.size());
        for (Note note : notes) {
            map.put(note.getId(), note);
        }
        List<Note> noteList = notePresenter.selectByIds(map.keySet());
        Map<String, Note> localMap = new HashMap<>(noteList.size());

        for (Note localNote : noteList) {
            localMap.put(localNote.getId(), localNote);
        }

        List<Note> addList = new ArrayList<>();
        List<Note> updateList = new ArrayList<>();
        for (String id : map.keySet()) {
            if (localMap.containsKey(id)) {
                if (map.get(id).getLastModifyTime().after(localMap.get(id).getLastModifyTime())) {
                    updateList.add(map.get(id));
                }
            }else {
                addList.add(map.get(id));
            }
        }
        notePresenter.addAll(addList);
        notePresenter.updateAll(updateList);

    }

    @Override
    public boolean isChanged(DataSource<Note> targetDataSource) throws IOException {

        return !getModifiedData(getCorrespondTraceInfo(targetDataSource)).isEmpty();

//        Date latestLastModifyTime = getLatestTraceInfo().getLastModifyTime();
//        Date correspondLastModifyTime = getCorrespondTraceInfo(targetDataSource).getLastModifyTime();
//        return !DateUtils.isSameInstant(latestLastModifyTime,correspondLastModifyTime);
    }

    @Override
    public TraceInfo getCorrespondTraceInfo(DataSource<Note> targetDataSource) throws IOException {
        SyncInfo syncInfo = MyApplication.getInstance().getDaoSession().getSyncInfoDao().queryBuilder()
                .where(SyncInfoDao.Properties.RemoteKey.eq(targetDataSource.getKey()))
                .where(SyncInfoDao.Properties.Type.eq(targetDataSource.clazz().getSimpleName().toLowerCase()))
                .unique();
        return syncInfo == null ? TraceInfo.ZERO : TraceInfo.create(syncInfo.getLastModifyTime(),syncInfo.getLastSyncTimeStart(), syncInfo.getLastSyncTime());
    }

    @Override
    public void setCorrespondTraceInfo(TraceInfo traceInfo, DataSource<Note> targetDataSource) throws IOException {
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

    private class MyNoteView extends NoteViewAdapter {
    }
}
