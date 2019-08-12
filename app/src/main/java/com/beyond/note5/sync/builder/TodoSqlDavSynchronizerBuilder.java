package com.beyond.note5.sync.builder;

import com.beyond.note5.bean.Account;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.dav.DavDataSourceProperty;
import com.beyond.note5.sync.datasource.dav.DefaultPointDavDataSource;
import com.beyond.note5.sync.datasource.sql.TodoSqlDataSourceWrap;
import com.beyond.note5.utils.OkWebDavUtil;

import static com.beyond.note5.MyApplication.DAV_ROOT_DIR;

/**
 * @author: beyond
 * @date: 2019/8/3
 */

public class TodoSqlDavSynchronizerBuilder extends AbstractPointSynchronizerBuilder<Todo> {


    public TodoSqlDavSynchronizerBuilder(Account account) {
        super(account);
    }

    @Override
    public DataSource<Todo> getDataSource1() {
        return new TodoSqlDataSourceWrap();
    }

    @Override
    public DataSource<Todo> getDataSource2() {
        DavDataSourceProperty property = new DavDataSourceProperty(
                account.getUsername(),
                account.getPassword(),
                OkWebDavUtil.concat(account.getServer(), DAV_ROOT_DIR)
        );

        boolean needExecutor =false;
        if (!account.getServer().contains("jianguoyun")){
            needExecutor = true;
        }
        property.setNeedExecutorService(needExecutor);

        return new DefaultPointDavDataSource<>(property,Todo.class);
    }
}
