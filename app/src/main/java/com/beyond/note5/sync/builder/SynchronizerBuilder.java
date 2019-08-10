package com.beyond.note5.sync.builder;

import com.beyond.note5.bean.Account;
import com.beyond.note5.bean.Tracable;
import com.beyond.note5.sync.DefaultSynchronizer;
import com.beyond.note5.sync.Synchronizer;
import com.beyond.note5.sync.context.SyncContext;
import com.beyond.note5.sync.context.SyncContextAware;
import com.beyond.note5.sync.context.SyncContextImpl;
import com.beyond.note5.sync.datasource.DataSource;

/**
 * @author: beyond
 * @date: 2019/8/3
 */

public abstract class SynchronizerBuilder<T extends Tracable> {
    protected Account account;

    public SynchronizerBuilder(Account account) {
        this.account = account;
    }

    public Synchronizer<T> build() {
        SyncContext context =  new SyncContextImpl();
        DataSource<T> dataSource1 = getDataSource1();
        DataSource<T> dataSource2 = getDataSource2();
        context.setDataSource1(dataSource1);
        context.setDataSource2(dataSource2);
        if (dataSource1 instanceof SyncContextAware){
            ((SyncContextAware) dataSource1).setContext(context);
        }
        if (dataSource2 instanceof SyncContextAware){
            ((SyncContextAware) dataSource2).setContext(context);
        }
        dataSource1.init();
        dataSource2.init();
        return new DefaultSynchronizer<>(context,dataSource1,dataSource2, null);
    }

    public abstract DataSource<T> getDataSource1();

    public abstract DataSource<T> getDataSource2();
}
