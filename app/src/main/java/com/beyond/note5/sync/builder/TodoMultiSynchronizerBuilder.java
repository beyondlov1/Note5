package com.beyond.note5.sync.builder;

import android.support.annotation.NonNull;

import com.beyond.note5.bean.Account;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.sync.datasource.MultiDataSource;
import com.beyond.note5.sync.datasource.dav.DavDataSourceProperty;
import com.beyond.note5.sync.datasource.dav.DefaultMultiDavDataSource;
import com.beyond.note5.sync.datasource.sql.TodoMultiSqlDataSource;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author: beyond
 * @date: 2019/8/9
 */

public class TodoMultiSynchronizerBuilder extends AbstractMultiSynchronizerBuilder<Todo> {

    public TodoMultiSynchronizerBuilder(List<Account> accounts) {
        super(accounts);
    }

    @NonNull
    @Override
    protected MultiDataSource<Todo> getMultiSqlDataSource() {
        return new TodoMultiSqlDataSource();
    }

    @NonNull
    @Override
    protected MultiDataSource<Todo> getMultiDavDataSource(ExecutorService executorService, DavDataSourceProperty property) {
        return new DefaultMultiDavDataSource<>(property, Todo.class, executorService);
    }
}
