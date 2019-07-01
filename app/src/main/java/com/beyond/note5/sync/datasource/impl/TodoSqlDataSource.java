package com.beyond.note5.sync.datasource.impl;


import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.model.dao.SyncInfoDao;
import com.beyond.note5.presenter.TodoPresenter;
import com.beyond.note5.presenter.TodoPresenterImpl;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.model.bean.SyncInfo;
import com.beyond.note5.sync.model.bean.TraceInfo;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.PreferenceUtil;
import com.beyond.note5.view.adapter.view.TodoViewAdapter;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class TodoSqlDataSource implements DataSource<Todo> {
    
    private TodoPresenter todoPresenter;

    public TodoSqlDataSource() {
        this.todoPresenter = new TodoPresenterImpl(new TodoSqlDataSource.MyTodoView());
    }

    public TodoSqlDataSource(TodoPresenter todoPresenter) {
        this.todoPresenter = todoPresenter;
    }

    @Override
    public String getKey() {
        return PreferenceUtil.getString(MyApplication.VIRTUAL_USER_ID);
    }

    @Override
    public void add(Todo todo) {
        todoPresenter.add(todo);
    }

    @Override
    public void delete(Todo todo) {
        todoPresenter.delete(todo);
    }

    @Override
    public void update(Todo todo) {
        todoPresenter.update(todo);
    }

    @Override
    public Todo select(Todo todo) {
        throw new RuntimeException("暂不支持");
    }

    @Override
    public Todo selectById(String id) throws IOException {
        return todoPresenter.selectById(id);
    }

    @Override
    public List<Todo> selectByIds(List<String> ids) {
        return todoPresenter.selectByIds(ids);
    }

    @Override
    public List<Todo> selectAll() throws IOException {
        return todoPresenter.selectAllInAll();
    }

    @Override
    public List<Todo> selectByModifiedDate(Date date) throws IOException {
        return todoPresenter.selectByModifiedDate(date);
    }


    @Override
    public TraceInfo getTraceInfo(DataSource<Todo> remoteDataSource) throws IOException {
        SyncInfo syncInfo = MyApplication.getInstance().getDaoSession().getSyncInfoDao().queryBuilder()
                .where(SyncInfoDao.Properties.RemoteKey.eq(remoteDataSource.getKey()))
                .where(SyncInfoDao.Properties.Type.eq(remoteDataSource.clazz().getSimpleName().toLowerCase()))
                .unique();
        return syncInfo == null?TraceInfo.ZERO:TraceInfo.create(syncInfo.getLastModifyTime(),syncInfo.getLastSyncTime());
    }

    @Override
    public void setTraceInfo(TraceInfo traceInfo, DataSource<Todo> remoteDataSource) throws IOException {
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
    public void cover(List<Todo> all) throws IOException {
        throw new RuntimeException("暂不支持");
    }

    @Override
    public Class<Todo> clazz() {
        return Todo.class;
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

    private class MyTodoView extends TodoViewAdapter {
    }
}
