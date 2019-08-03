package com.beyond.note5.sync.builder;

import com.beyond.note5.bean.Account;
import com.beyond.note5.bean.Tracable;
import com.beyond.note5.sync.DefaultSynchronizer;
import com.beyond.note5.sync.Synchronizer;
import com.beyond.note5.sync.context.SyncContext;
import com.beyond.note5.sync.context.SyncContextImpl;
import com.beyond.note5.sync.datasource.DataSource;

/**
 * @author: beyond
 * @date: 2019/8/3
 */

public abstract class SynchronizerBuilder<T extends Tracable> {

    protected String key1;
    protected String key2;
    protected Account account;

    public SynchronizerBuilder(String key1, String key2, Account account) {
        this.key1 = key1;
        this.key2 = key2;
        this.account = account;
    }

    public Synchronizer<T> build() {
        SyncContext context = getContext();
        DataSource<T> dataSource1 = getDataSource1();
        DataSource<T> dataSource2 = getDataSource2();
        context.setDataSource1(dataSource1);
        context.setDataSource2(dataSource2);
        dataSource1.setContext(context);
        dataSource2.setContext(context);
        return new DefaultSynchronizer<>(context,dataSource1,dataSource2, null);
    }

    public SyncContext getContext() {
        return new SyncContextImpl(key1,key2);
    }

    public abstract DataSource<T> getDataSource1();

    public abstract DataSource<T> getDataSource2();
}
