package com.beyond.note5.model;

import android.content.Context;
import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.model.dao.DaoSession;
import com.beyond.note5.model.dao.ReminderDao;
import com.beyond.note5.model.dao.TodoDao;

import java.util.List;

public class TodoModelImpl implements TodoModel {

    public static final String IS_SHOW_READ_FLAG_DONE = "IS_SHOW_READ_FLAG_DONE";

    private TodoDao todoDao;
    private ReminderDao reminderDao;

    public TodoModelImpl() {
        DaoSession daoSession = MyApplication.getInstance().getDaoSession();
        todoDao = daoSession.getTodoDao();
        reminderDao = daoSession.getReminderDao();
    }

    @Override
    public void add(Todo todo) {
        todoDao.insert(todo);
        if (todo.getReminder() != null) {
            reminderDao.insert(todo.getReminder());
        }

        //train
        MyApplication.getInstance().getTagPredictor().getTagTrainer().train(todo.getContent());
    }

    @Override
    public void update(Todo todo) {
        todoDao.update(todo);
        if (todo.getReminder() != null) {
            reminderDao.update(todo.getReminder());
        }

        //train
        MyApplication.getInstance().getTagPredictor().getTagTrainer().train(todo.getContent());
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
//        if (MyApplication.getInstance().getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
//                .getBoolean(IS_SHOW_READ_FLAG_DONE, false)) {
//            return todoQueryBuilder.list();
//        } else {
//            todoQueryBuilder.where(TodoDao.Properties.ReadFlag.eq(DocumentConst.READ_FLAG_NORMAL));
//            return todoQueryBuilder.list();
//        }


        if (MyApplication.getInstance().getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getBoolean(IS_SHOW_READ_FLAG_DONE, false)) {
            return todoDao.queryDeep("WHERE T.TYPE = 'todo' " +
                    "ORDER BY CASE WHEN T0.START IS NULL THEN DATE('now') ELSE DATE( T0.START/1000,'unixepoch','localtime') END ASC," +
                    "READ_FLAG ASC, " +
                    "CASE WHEN T0.START IS NULL THEN 1 ELSE 0 END ASC," +
                    "T0.START ASC," +
                    "LAST_MODIFY_TIME DESC"
            );
        } else {
            return todoDao.queryDeep("WHERE T.TYPE = 'todo' AND T.READ_FLAG = 0 " +
                    "ORDER BY CASE WHEN T0.START IS NULL THEN DATE('now') ELSE DATE( T0.START/1000,'unixepoch','localtime') END ASC," +
                    "READ_FLAG ASC, " +
                    "CASE WHEN T0.START IS NULL THEN 1 ELSE 0 END ASC," +
                    "T0.START ASC," +
                    "LAST_MODIFY_TIME DESC"
            );
        }

    }
}
