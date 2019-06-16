package com.beyond.note5.sync.datasource;

import com.beyond.note5.bean.Tracable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import static com.beyond.note5.utils.AsyncUtil.computeAllAsyn;
import static com.beyond.note5.utils.AsyncUtil.computeAsyn;

public abstract class DavDataSourceComposite<T extends Tracable> implements DataSource<T> {

    private DataSource<T>[] subDataSources;

    private ExecutorService executorService;

    public DavDataSourceComposite(DataSource<T>... subDataSources) {
        this.subDataSources = subDataSources;
    }

    public DavDataSourceComposite(ExecutorService executorService, DataSource<T>... subDataSources) {
        this.subDataSources = subDataSources;
        this.executorService = executorService;
    }

    @Override
    public void add(T t) throws IOException {
        chooseDataSource(t.getId()).add(t);
    }

    private DataSource<T> chooseDataSource(String id) {
        if (subDataSources.length == 0) {
            throw new RuntimeException("url不能为空");
        }
        if (subDataSources.length == 1) {
            return subDataSources[0];
        }

        int index = Math.abs(id.hashCode()/10) % subDataSources.length;
        return subDataSources[index];
    }

    @Override
    public void delete(T t) throws IOException {
        chooseDataSource(t.getId()).delete(t);
    }

    @Override
    public void update(T t) throws IOException {
        chooseDataSource(t.getId()).update(t);

    }

    @Override
    public T select(T t) throws IOException {
        return selectById(t.getId());
    }

    @Override
    public T selectById(String id) throws IOException {
        return chooseDataSource(id).selectById(id);
    }

    @Override
    public List<T> selectAll() throws IOException, ExecutionException, InterruptedException {

        if (executorService != null) {
            List<Callable<List<T>>> callables = new ArrayList<>();
            for (DataSource<T> subDataSource : subDataSources) {
                callables.add(new Callable<List<T>>() {
                    @Override
                    public List<T> call() throws Exception {
                        return subDataSource.selectAll();
                    }
                });
            }
            return computeAllAsyn(executorService, callables);
        } else {
            List<T> result = new ArrayList<>();
            for (DataSource<T> subDataSource : subDataSources) {
                result.addAll(subDataSource.selectAll());
            }
            return result;
        }
    }


    @Override
    public void cover(List<T> all) throws IOException, ExecutionException, InterruptedException {
        // 改多线程？
        if (executorService != null) {
            List<Callable<T>> callables = new ArrayList<>();
            for (T t : all) {
                callables.add(new Callable<T>() {
                    @Override
                    public T call() throws Exception {
                        add(t);
                        return null;
                    }
                });
            }
            computeAsyn(executorService, callables);
        } else {
            for (T t : all) {
                add(t);
            }
        }
    }

}
