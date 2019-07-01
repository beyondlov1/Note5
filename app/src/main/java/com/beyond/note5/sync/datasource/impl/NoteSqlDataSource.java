package com.beyond.note5.sync.datasource.impl;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Note;
import com.beyond.note5.event.note.AddNoteSuccessEvent;
import com.beyond.note5.event.note.DeleteNoteSuccessEvent;
import com.beyond.note5.model.dao.SyncInfoDao;
import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.presenter.NotePresenterImpl;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.model.bean.SyncInfo;
import com.beyond.note5.sync.model.bean.TraceInfo;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.PreferenceUtil;
import com.beyond.note5.view.adapter.view.NoteViewAdapter;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class NoteSqlDataSource implements DataSource<Note> {

    private NotePresenter notePresenter;

    public NoteSqlDataSource() {
        this.notePresenter = new NotePresenterImpl(new MyNoteView());
    }

    public NoteSqlDataSource(NotePresenter notePresenter) {
        this.notePresenter = notePresenter;
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
        if (!oldNote.getValid() && note.getValid()){
            EventBus.getDefault().post(new AddNoteSuccessEvent(note));
        }else if (oldNote.getValid() && !note.getValid()){
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
    public TraceInfo getTraceInfo(DataSource<Note> remoteDataSource) throws IOException {
        SyncInfo syncInfo = MyApplication.getInstance().getDaoSession().getSyncInfoDao().queryBuilder()
                .where(SyncInfoDao.Properties.RemoteKey.eq(remoteDataSource.getKey()))
                .where(SyncInfoDao.Properties.Type.eq(remoteDataSource.clazz().getSimpleName().toLowerCase()))
                .unique();
        return syncInfo == null?TraceInfo.ZERO:TraceInfo.create(syncInfo.getLastModifyTime(),syncInfo.getLastSyncTime());
    }

    @Override
    public void setTraceInfo(TraceInfo traceInfo, DataSource<Note> remoteDataSource) throws IOException {
        SyncInfoDao syncInfoDao = MyApplication.getInstance().getDaoSession().getSyncInfoDao();
        SyncInfo syncInfo = syncInfoDao.queryBuilder()
                .where(SyncInfoDao.Properties.RemoteKey.eq(remoteDataSource.getKey()))
                .where(SyncInfoDao.Properties.Type.eq(remoteDataSource.clazz().getSimpleName().toLowerCase()))
                .unique();
        if (syncInfo == null){
            SyncInfo info = new SyncInfo();
            info.setId(IDUtil.uuid());
            info.setLocalKey(getKey());
            info.setRemoteKey(remoteDataSource.getKey());
            info.setLastModifyTime(traceInfo.getLastModifyTime());
            info.setLastSyncTime(traceInfo.getLastSyncTime());
            info.setType(remoteDataSource.clazz().getSimpleName().toLowerCase());
            syncInfoDao.insert(info);
        }else {
            syncInfo.setLastModifyTime(traceInfo.getLastModifyTime());
            syncInfo.setLastSyncTime(traceInfo.getLastSyncTime());
            syncInfoDao.update(syncInfo);
        }
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
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(Long time) {
        return false;
    }

    @Override
    public boolean isLocked() {
        return false;
    }

    @Override
    public boolean release() {
        return false;
    }

    private class MyNoteView extends NoteViewAdapter {
    }
}
