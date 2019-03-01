package com.beyond.note5.model;

import android.content.Context;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Document;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.constant.DocumentConst;
import com.beyond.note5.model.dao.DaoSession;
import com.beyond.note5.model.dao.TodoDao;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

public class TodoModelImpl implements TodoModel {

    public static final String IS_SHOW_READ_FLAG_DONE = "IS_SHOW_READ_FLAG_DONE";

    private TodoDao todoDao;

    public TodoModelImpl() {
        DaoSession daoSession = MyApplication.getInstance().getDaoSession();
        todoDao = daoSession.getTodoDao();
    }

    @Override
    public void add(Todo todo) {
        todoDao.insert(todo);
    }

    @Override
    public void update(Todo todo) {
        todoDao.update(todo);
    }

    @Override
    public void delete(Todo todo) {
        todoDao.delete(todo);
    }

    @Override
    public List<Todo> findAll() {
        QueryBuilder<Todo> todoQueryBuilder = todoDao.queryBuilder();
        todoQueryBuilder
                .where(TodoDao.Properties.Type.eq(Document.TODO))
                .orderRaw("DATE(LAST_MODIFY_TIME/1000,'unixepoch','localtime') DESC,READ_FLAG ASC, LAST_MODIFY_TIME DESC");
        if (MyApplication.getInstance().getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getBoolean(IS_SHOW_READ_FLAG_DONE,false)){
            return todoQueryBuilder.list();
        }else {
            todoQueryBuilder.where(TodoDao.Properties.ReadFlag.eq(DocumentConst.READ_FLAG_NORMAL));
            return todoQueryBuilder.list();
        }
    }
}
