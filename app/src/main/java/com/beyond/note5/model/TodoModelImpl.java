package com.beyond.note5.model;

import android.content.Context;
import android.support.annotation.NonNull;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Document;
import com.beyond.note5.bean.Reminder;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.model.dao.DaoSession;
import com.beyond.note5.model.dao.ReminderDao;
import com.beyond.note5.model.dao.SyncLogInfoDao;
import com.beyond.note5.model.dao.SyncStateInfoDao;
import com.beyond.note5.model.dao.TodoDao;
import com.beyond.note5.sync.model.bean.SyncLogInfo;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.PreferenceUtil;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class TodoModelImpl implements TodoModel {

    public static final String IS_SHOW_READ_FLAG_DONE = "IS_SHOW_READ_FLAG_DONE";

    private TodoDao todoDao;
    private ReminderDao reminderDao;
    private SyncLogInfoDao syncLogInfoDao;
    private SyncStateInfoDao syncStateInfoDao;

    public static TodoModel getSingletonInstance() {
        return TodoModelHolder.TODO_MODEL;
    }

    private static class TodoModelHolder {
        private static final TodoModel TODO_MODEL = new TodoModelImpl();
    }

    public TodoModelImpl() {
        DaoSession daoSession = MyApplication.getInstance().getDaoSession();
        todoDao = daoSession.getTodoDao();
        reminderDao = daoSession.getReminderDao();
        syncLogInfoDao = daoSession.getSyncLogInfoDao();
        syncStateInfoDao = daoSession.getSyncStateInfoDao();
    }

    @Override
    public void add(Todo todo) {
        todoDao.insert(todo);
        if (todo.getReminder() != null) {
            reminderDao.insert(todo.getReminder());
        }
        onInserted(todo,PreferenceUtil.getString(MyApplication.VIRTUAL_USER_ID));
    }

    @Override
    public void update(Todo todo) {
        todoDao.update(todo);

        Reminder reminder = todo.getReminder();
        if (todo.getReminderId() != null) {
            Reminder foundReminder = reminderDao.queryBuilder()
                    .where(ReminderDao.Properties.Id.eq(todo.getReminderId()))
                    .build()
                    .unique();
            if (foundReminder == null) {
                reminderDao.insert(reminder);
            } else {
                reminderDao.update(reminder);
            }
        }

        onUpdated(todo,PreferenceUtil.getString(MyApplication.VIRTUAL_USER_ID));
    }

    @Override
    public void deleteLogic(Todo todo) {
        todo.setLastModifyTime(new Date());
        todo.setValid(false);
        todoDao.update(todo);

        onUpdated(todo,PreferenceUtil.getString(MyApplication.VIRTUAL_USER_ID));
    }


    @Override
    public void delete(Todo todo) {
        todoDao.delete(todo);
        if (todo.getReminder() != null) {
            reminderDao.delete(todo.getReminder());
        }
    }

    @Override
    public List<Todo> findAll() {
//        QueryBuilder<Todo> todoQueryBuilder = todoDao.queryBuilder();
//        todoQueryBuilder
//                .where(TodoDao.Properties.Type.eq(Document.TODO))
//                .orderRaw("DATE(LAST_MODIFY_TIME/1000,'unixepoch','localtime') DESC,READ_FLAG ASC, LAST_MODIFY_TIME DESC");
//        if (MyApplication.create().getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
//                .getBoolean(IS_SHOW_READ_FLAG_DONE, false)) {
//            return todoQueryBuilder.list();
//        } else {
//            todoQueryBuilder.where(TodoDao.Properties.ReadFlag.eq(DocumentConst.READ_FLAG_NORMAL));
//            return todoQueryBuilder.list();
//        }


        if (MyApplication.getInstance().getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getBoolean(IS_SHOW_READ_FLAG_DONE, false)) {
            return todoDao.queryDeep("WHERE T.TYPE = 'todo' AND T.VALID = '1' " +
                    "ORDER BY CASE WHEN T0.START IS NULL THEN DATE('now','localtime') ELSE DATE( T0.START/1000,'unixepoch','localtime') END ASC," +
                    "READ_FLAG ASC, " +
                    "CASE WHEN T0.START IS NULL THEN 1 ELSE 0 END ASC," +
                    "T0.START ASC," +
                    "LAST_MODIFY_TIME DESC"
            );
        } else {
            return todoDao.queryDeep("WHERE T.TYPE = 'todo' AND T.READ_FLAG = 0 AND T.VALID = '1' " +
                    "ORDER BY CASE WHEN T0.START IS NULL THEN DATE('now','localtime') ELSE DATE( T0.START/1000,'unixepoch','localtime') END ASC," +
                    "READ_FLAG ASC, " +
                    "CASE WHEN T0.START IS NULL THEN 1 ELSE 0 END ASC," +
                    "T0.START ASC," +
                    "LAST_MODIFY_TIME DESC"
            );
        }

    }

    @Override
    public List<Todo> findAllInAll() {
        return todoDao.queryBuilder()
                .where(TodoDao.Properties.Type.eq(Document.TODO))
                .list();
    }

    @Override
    public Todo findById(String id) {
        return todoDao.load(id);
    }

    @Override
    public List<Todo> findAllAfterLastModifyTime(Date date) {
        return todoDao.queryBuilder()
                .where(TodoDao.Properties.Type.eq(Document.TODO))
                .where(TodoDao.Properties.LastModifyTime.gt(date))
                .orderAsc(TodoDao.Properties.ReadFlag)
                .orderDesc(TodoDao.Properties.LastModifyTime)
                .list();
    }

    @Override
    public List<Todo> findByIds(Collection<String> ids) {
        return todoDao.queryBuilder()
                .where(TodoDao.Properties.Id.in(ids))
                .list();
    }

    @Override
    public void deleteReminder(Todo todo) {
        Reminder reminder = todo.getReminder();
        if (reminder != null) {
            reminderDao.delete(reminder);
        }
    }

    @Override
    public void addAll(List<Todo> addList) {
       addAll(addList,PreferenceUtil.getString(MyApplication.VIRTUAL_USER_ID));
    }

    @Override
    public void addAll(List<Todo> addList, String source) {
        todoDao.insertInTx(addList);

        // 只是插入记录, 不会添加提醒
        List<Reminder> allReminders = new ArrayList<>();
        for (Todo todo : addList) {
            if (todo.getReminder() != null) {
                allReminders.add(todo.getReminder());
            }
        }

        if (CollectionUtils.isNotEmpty(allReminders)) {
            reminderDao.insertInTx(allReminders);
        }

        onInsertedAll(source,addList.toArray(new Todo[0]));
    }

    @Override
    public void updateAll(List<Todo> updateList) {
        updateAll(updateList, PreferenceUtil.getString(MyApplication.VIRTUAL_USER_ID));
    }

    @Override
    public void updateAll(List<Todo> updateList, String source) {
        todoDao.updateInTx(updateList);

        // 只是插入记录, 不会添加提醒
        List<Reminder> allReminders = new ArrayList<>();
        for (Todo todo : updateList) {
            if (todo.getReminder() != null) {
                allReminders.add(todo.getReminder());
            }
        }

        if (CollectionUtils.isNotEmpty(allReminders)) {
            reminderDao.updateInTx(allReminders);
        }

        onUpdatedAll(source,updateList.toArray(new Todo[0]));
    }




    private void onInserted(Todo todo, String source) {
        addInsertLog(todo,source);
    }

    private void onUpdated(Todo todo, String source) {
        addUpdateLog(todo,source);
        removeSyncSuccessStateInfo(todo);
    }

    private void onInsertedAll(String source,Todo... addList) {
        addAllInsertLog(source,addList);
    }

    private void onUpdatedAll(String source,Todo... updateList) {
        addAllUpdateLog( source,updateList);
    }



    private void addAllInsertLog(String source, Todo... todos) {
        List<SyncLogInfo> syncLogInfos = new ArrayList<>(todos.length);
        for (Todo todo : todos) {
            syncLogInfos.add(createAddSyncLogInfo(todo,source));
        }
        syncLogInfoDao.insertInTx(syncLogInfos);
    }

    private void addAllUpdateLog( String source,Todo... todos) {
        List<SyncLogInfo> syncLogInfos = new ArrayList<>(todos.length);
        for (Todo todo : todos) {
            syncLogInfos.add(createUpdateSyncLogInfo(todo,source));
        }
        syncLogInfoDao.insertInTx(syncLogInfos);
    }

    private void addInsertLog(Todo todo, String source) {
        SyncLogInfo syncLogInfo = createAddSyncLogInfo(todo,source);
        syncLogInfoDao.insert(syncLogInfo);
    }

    private void addUpdateLog(Todo todo,String source) {
        SyncLogInfo syncLogInfo = createUpdateSyncLogInfo(todo,source);
        syncLogInfoDao.insert(syncLogInfo);
    }

    @NonNull
    private SyncLogInfo createAddSyncLogInfo(Todo todo,String source) {
        SyncLogInfo syncLogInfo = new SyncLogInfo();
        syncLogInfo.setId(IDUtil.uuid());
        syncLogInfo.setDocumentId(todo.getId());
        syncLogInfo.setOperation(SyncLogInfo.ADD);
        syncLogInfo.setOperationTime(todo.getLastModifyTime());
        syncLogInfo.setCreateTime(new Date());
        syncLogInfo.setSource(source);
        syncLogInfo.setType(Todo.class.getSimpleName().toLowerCase());
        return syncLogInfo;
    }

    @NonNull
    private SyncLogInfo createUpdateSyncLogInfo(Todo todo,String source) {
        SyncLogInfo syncLogInfo = new SyncLogInfo();
        syncLogInfo.setId(IDUtil.uuid());
        syncLogInfo.setDocumentId(todo.getId());
        syncLogInfo.setOperation(SyncLogInfo.UPDATE);
        syncLogInfo.setOperationTime(todo.getLastModifyTime());
        syncLogInfo.setCreateTime(new Date());
        syncLogInfo.setSource(source);
        syncLogInfo.setType(Todo.class.getSimpleName().toLowerCase());
        return syncLogInfo;
    }

    private void removeSyncSuccessStateInfo(Todo todo) {
        syncStateInfoDao.queryBuilder()
                .where(SyncStateInfoDao.Properties.DocumentId.eq(todo.getId()))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
    }
}
