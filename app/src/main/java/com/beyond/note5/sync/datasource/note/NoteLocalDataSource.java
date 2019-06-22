package com.beyond.note5.sync.datasource.note;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.model.bean.SyncInfo;
import com.beyond.note5.model.dao.SyncInfoDao;
import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.presenter.NotePresenterImpl;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.view.adapter.view.NoteViewAdapter;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class NoteLocalDataSource implements DataSource<Note> {

    private NotePresenter notePresenter;

    private final SyncInfoDao syncInfoDao;

    public NoteLocalDataSource() {
        this.notePresenter = new NotePresenterImpl(new MyNoteView());
        syncInfoDao = MyApplication.getInstance().getDaoSession().getSyncInfoDao();
    }

    public NoteLocalDataSource(NotePresenter notePresenter) {
        this.notePresenter = notePresenter;
        syncInfoDao = MyApplication.getInstance().getDaoSession().getSyncInfoDao();
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
        notePresenter.update(note);
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
    public List<Note> selectAll() throws IOException {
        return notePresenter.selectAllInAll();
    }

    @Override
    public void cover(List<Note> all) throws IOException {
        throw new RuntimeException("暂不支持");
    }

    @Override
    public String getKey() {
        return null;
    }

    public Date getLastSyncTime(String syncTargetKey) {
        SyncInfo syncInfo = syncInfoDao.queryBuilder()
                .where(SyncInfoDao.Properties.RemoteKey.eq(syncTargetKey))
                .unique();
        if (syncInfo!=null){
            return syncInfo.getLastSyncTime();
        }else {
            return new Date(0);
        }
    }

    public void setLastSyncTime(String syncTargetKey, Date date) throws IOException {
        SyncInfo syncInfo = syncInfoDao.queryBuilder()
                .where(SyncInfoDao.Properties.RemoteKey.eq(syncTargetKey))
                .unique();
        if (syncInfo == null){
            SyncInfo info = new SyncInfo();
            info.setId(IDUtil.uuid());
            info.setRemoteKey(syncTargetKey);
            info.setLastSyncTime(date);
            syncInfoDao.insert(info);
        }else {
            syncInfoDao.update(syncInfo);
        }
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
