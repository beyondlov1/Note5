package com.beyond.note5.model;

import android.content.Context;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Reminder;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.model.dao.DaoSession;
import com.beyond.note5.model.dao.ReminderDao;
import com.beyond.note5.model.dao.SyncLogInfoDao;
import com.beyond.note5.model.dao.TodoDao;
import com.beyond.note5.sync.model.bean.SyncLogInfo;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.PreferenceUtil;

import java.util.Date;
import java.util.List;

public class TodoModelImpl implements TodoModel {

    public static final String IS_SHOW_READ_FLAG_DONE = "IS_SHOW_READ_FLAG_DONE";

    private TodoDao todoDao;
    private ReminderDao reminderDao;
    private SyncLogInfoDao syncLogInfoDao;

    public static TodoModel getSingletonInstance(){
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
    }

    @Override
    public void add(Todo todo) {
        todoDao.insert(todo);
        if (todo.getReminder() != null) {
            reminderDao.insert(todo.getReminder());
        }

        addInsertLog(todo);
    }

    @Override
    public void update(Todo todo) {
        todoDao.update(todo);

        Reminder reminder = todo.getReminder();
        if (todo.getReminderId()!=null) {
            Reminder foundReminder = reminderDao.queryBuilder()
                    .where(ReminderDao.Properties.Id.eq(todo.getReminderId()))
                    .build()
                    .unique();
            if (foundReminder == null){
                reminderDao.insert(reminder);
            }else {
                reminderDao.update(reminder);
            }
        }


        addUpdateLog(todo);

    }

    @Override
    public void deleteLogic(Todo todo) {
        todo.setLastModifyTime(new Date());
        todo.setValid(false);
        todoDao.update(todo);

        addUpdateLog(todo);

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
        if (MyApplication.getInstance().getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getBoolean(IS_SHOW_READ_FLAG_DONE, false)) {
            return todoDao.queryDeep("WHERE T.TYPE = 'todo' " +
                    "ORDER BY CASE WHEN T0.START IS NULL THEN DATE('now','localtime') ELSE DATE( T0.START/1000,'unixepoch','localtime') END ASC," +
                    "READ_FLAG ASC, " +
                    "CASE WHEN T0.START IS NULL THEN 1 ELSE 0 END ASC," +
                    "T0.START ASC," +
                    "LAST_MODIFY_TIME DESC"
            );
        } else {
            return todoDao.queryDeep("WHERE T.TYPE = 'todo' AND T.READ_FLAG = 0 " +
                    "ORDER BY CASE WHEN T0.START IS NULL THEN DATE('now','localtime') ELSE DATE( T0.START/1000,'unixepoch','localtime') END ASC," +
                    "READ_FLAG ASC, " +
                    "CASE WHEN T0.START IS NULL THEN 1 ELSE 0 END ASC," +
                    "T0.START ASC," +
                    "LAST_MODIFY_TIME DESC"
            );
        }
    }

    @Override
    public Todo findById(String id) {
        return todoDao.load(id);
    }

    @Override
    public void deleteReminder(Todo todo) {
        Reminder reminder = todo.getReminder();
        if (reminder !=null){
            reminderDao.delete(reminder);
        }
    }

    private void addInsertLog(Todo todo){
        SyncLogInfo syncLogInfo = new SyncLogInfo();
        syncLogInfo.setId(IDUtil.uuid());
        syncLogInfo.setDocumentId(todo.getId());
        syncLogInfo.setOperation(SyncLogInfo.ADD);
        syncLogInfo.setOperationTime(todo.getLastModifyTime());
        syncLogInfo.setSource(PreferenceUtil.getString(MyApplication.VIRTUAL_USER_ID));
        syncLogInfoDao.insert(syncLogInfo);
    }

    private void addUpdateLog(Todo todo){
        SyncLogInfo syncLogInfo = new SyncLogInfo();
        syncLogInfo.setId(IDUtil.uuid());
        syncLogInfo.setDocumentId(todo.getId());
        syncLogInfo.setOperation(SyncLogInfo.UPDATE);
        syncLogInfo.setOperationTime(todo.getLastModifyTime());
        syncLogInfo.setSource(PreferenceUtil.getString(MyApplication.VIRTUAL_USER_ID));
        syncLogInfoDao.insert(syncLogInfo);
    }

}
