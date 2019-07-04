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

import org.apache.commons.lang3.time.DateUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public TraceInfo getLatestTraceInfo() throws IOException {
        List<Todo> list = selectAll();
        if (list != null && !list.isEmpty()) {
            Collections.sort(list, new Comparator<Todo>() {
                @Override
                public int compare(Todo o1, Todo o2) {
                    return (int) (o1.getLastModifyTime().getTime() - o2.getLastModifyTime().getTime());
                }
            });
            Todo latestTodo = list.get(list.size()-1);
            return TraceInfo.create(latestTodo.getLastModifyTime(), latestTodo.getLastModifyTime());
        } else {
            return TraceInfo.ZERO;
        }
    }

    @Override
    public void setLatestTraceInfo(TraceInfo traceInfo) throws IOException {
        //do nothing
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
    public List<Todo> getModifiedData(TraceInfo traceInfo) throws IOException {
        return selectByModifiedDate(traceInfo.getLastModifyTime());
    }

    @Override
    public void save(Todo todo) throws IOException {
        Todo localTodo = todoPresenter.selectById(todo.getId());
        if (localTodo != null) {
            if (todo.getLastModifyTime().after(localTodo.getLastModifyTime())) {
                update(todo);
            }
        } else {
            add(todo);
        }
    }

    @Override
    public void saveAll(List<Todo> todos) throws IOException {
        Map<String, Todo> map = new HashMap<>(todos.size());
        for (Todo todo : todos) {
            map.put(todo.getId(), todo);
        }
        List<Todo> todoList = todoPresenter.selectByIds(map.keySet());
        Map<String, Todo> localMap = new HashMap<>(todoList.size());

        for (Todo localTodo : todoList) {
            localMap.put(localTodo.getId(), localTodo);
        }

        for (String id : map.keySet()) {
            if (localMap.containsKey(id)) {
                if (map.get(id).getLastModifyTime().after(localMap.get(id).getLastModifyTime())) {
                    update(map.get(id));
                }
            } else {
                add(map.get(id));
            }
        }
    }

    @Override
    public boolean isChanged(DataSource<Todo> targetDataSource) throws IOException {
        Date latestLastModifyTime = getLatestTraceInfo().getLastModifyTime();
        Date correspondLastModifyTime = getCorrespondTraceInfo(targetDataSource).getLastModifyTime();
        return !DateUtils.isSameInstant(latestLastModifyTime, correspondLastModifyTime);
    }

    @Override
    public TraceInfo getCorrespondTraceInfo(DataSource<Todo> targetDataSource) throws IOException {
        SyncInfo syncInfo = MyApplication.getInstance().getDaoSession().getSyncInfoDao().queryBuilder()
                .where(SyncInfoDao.Properties.RemoteKey.eq(targetDataSource.getKey()))
                .where(SyncInfoDao.Properties.Type.eq(targetDataSource.clazz().getSimpleName().toLowerCase()))
                .unique();
        return syncInfo == null ? TraceInfo.ZERO : TraceInfo.create(syncInfo.getLastModifyTime(), syncInfo.getLastSyncTime());
    }

    @Override
    public void setCorrespondTraceInfo(TraceInfo traceInfo, DataSource<Todo> targetDataSource) throws IOException {
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
            info.setType(targetDataSource.clazz().getSimpleName().toLowerCase());
            syncInfoDao.insert(info);
        } else {
            syncInfo.setLastModifyTime(traceInfo.getLastModifyTime());
            syncInfo.setLastSyncTime(traceInfo.getLastSyncTimeEnd());
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

    private class MyTodoView extends TodoViewAdapter {
    }
}
