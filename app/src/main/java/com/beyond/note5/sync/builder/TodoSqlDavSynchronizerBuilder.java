package com.beyond.note5.sync.builder;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Account;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.dav.DavPathStrategy;
import com.beyond.note5.sync.datasource.dav.DefaultDavDataSource;
import com.beyond.note5.sync.datasource.dav.UuidDavPathStrategy;
import com.beyond.note5.sync.datasource.sql.TodoSqlDataSourceWrap;
import com.beyond.note5.sync.webdav.client.DavClient;
import com.beyond.note5.sync.webdav.client.SardineDavClient;
import com.beyond.note5.utils.OkWebDavUtil;

import java.util.concurrent.ExecutorService;

import static com.beyond.note5.MyApplication.DAV_ROOT_DIR;

/**
 * @author: beyond
 * @date: 2019/8/3
 */

public class TodoSqlDavSynchronizerBuilder extends SynchronizerBuilder<Todo> {

    public TodoSqlDavSynchronizerBuilder(String key1, String key2, Account account) {
        super(key1, key2, account);
    }

    @Override
    public DataSource<Todo> getDataSource1() {
        return new TodoSqlDataSourceWrap(key2);
    }

    @Override
    public DataSource<Todo> getDataSource2() {
        String serverWithPath = OkWebDavUtil.concat( account.getServer(), DAV_ROOT_DIR);
        DavClient davClient = new SardineDavClient(account.getUsername(), account.getPassword());
        DavPathStrategy pathStrategy = new UuidDavPathStrategy(serverWithPath,Todo.class);
        ExecutorService executorService = null;
        if (!account.getServer().contains("jianguoyun")){
            executorService =  MyApplication.getInstance().getExecutorService();
        }
        return new DefaultDavDataSource<>(key1, davClient, serverWithPath,
                pathStrategy,executorService,Todo.class);
    }
}
