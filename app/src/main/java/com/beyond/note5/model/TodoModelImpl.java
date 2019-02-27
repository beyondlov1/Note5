package com.beyond.note5.model;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Document;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.model.dao.DaoSession;
import com.beyond.note5.model.dao.TodoDao;

import java.util.List;

public class TodoModelImpl implements TodoModel {

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
        return todoDao.queryBuilder()
                .where(TodoDao.Properties.Type.eq(Document.TODO))
                .orderAsc(TodoDao.Properties.ReadFlag)
//                .orderRaw("READ_FLAG ASC,LAST_MODIFY_TIME DESC")
                .orderDesc(TodoDao.Properties.LastModifyTime)
                .list();
    }
}
