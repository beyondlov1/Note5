package com.beyond.note5.sync.builder;

import android.support.annotation.NonNull;

import com.beyond.note5.bean.Account;
import com.beyond.note5.bean.Tracable;
import com.beyond.note5.sync.DefaultMultiSynchronizer;
import com.beyond.note5.sync.Synchronizer;
import com.beyond.note5.sync.datasource.MultiDataSource;
import com.beyond.note5.sync.datasource.dav.DavDataSourceProperty;
import com.beyond.note5.utils.OkWebDavUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.beyond.note5.MyApplication.CPU_COUNT;
import static com.beyond.note5.MyApplication.DAV_ROOT_DIR;

/**
 * @author: beyond
 * @date: 2019/8/10
 */

public abstract class AbstractMultiSynchronizerBuilder<T extends Tracable> {
    private final List<Account> accounts;

    public AbstractMultiSynchronizerBuilder(List<Account> accounts) {
        this.accounts = accounts;
    }

    public Synchronizer<T> build() {

        ExecutorService executorService = new ThreadPoolExecutor(
                CPU_COUNT*2+1, 60,
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());

        List<MultiDataSource<T>> dataSources = new ArrayList<>();
        for (Account account : accounts) {
            DavDataSourceProperty property = new DavDataSourceProperty(
                    account.getUsername(),
                    account.getPassword(),
                    OkWebDavUtil.concat(account.getServer(), DAV_ROOT_DIR)
            );

            boolean needExecutor = false;
            if (!account.getServer().contains("jianguoyun")) {
                needExecutor = true;
            }
            property.setNeedExecutorService(needExecutor);

            dataSources.add(getMultiDavDataSource(executorService, property));
        }

        dataSources.add(getMultiSqlDataSource());

        for (MultiDataSource<T> dataSource : dataSources) {
            dataSource.init();
        }

        return new DefaultMultiSynchronizer<T>(dataSources, executorService);

    }

    @NonNull
    protected abstract MultiDataSource<T> getMultiSqlDataSource() ;

    @NonNull
    protected abstract MultiDataSource<T> getMultiDavDataSource(ExecutorService executorService, DavDataSourceProperty property);
}
