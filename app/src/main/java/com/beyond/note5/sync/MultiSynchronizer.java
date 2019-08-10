package com.beyond.note5.sync;

import android.util.Log;

import com.beyond.note5.bean.Tracable;
import com.beyond.note5.sync.datasource.MultiDataSource;
import com.beyond.note5.sync.datasource.entity.SyncStamp;
import com.beyond.note5.sync.exception.SaveException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.beyond.note5.MyApplication.CPU_COUNT;
import static com.beyond.note5.sync.SyncUtils.blockExecute;

/**
 * @author: beyond
 * @date: 2019/8/9
 */
//TODO: STATE
public class MultiSynchronizer<T extends Tracable> implements Synchronizer<T> {

    private List<MultiDataSource<T>> dataSources;

    private ExecutorService executorService;

    private ExecutorService executorServiceExtra;  // 防止多线程嵌套时死锁

//    private MultiSyncContext context;

    private Date syncTimeStart;

    public MultiSynchronizer(List<MultiDataSource<T>> dataSources, ExecutorService executorService) {
        this.dataSources = dataSources;
        this.executorService = executorService;
        this.executorServiceExtra = new ThreadPoolExecutor(
                CPU_COUNT*2+1, 60,
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
    }

    @Override
    public boolean sync() throws Exception {

        syncTimeStart = new Date();

        List<MultiDataSource<T>> dataSources = new ArrayList<>(this.dataSources);

        Log.d(getClass().getSimpleName(), "initSyncStamps:" + new Date());
        initSyncStamps(dataSources);

        Log.d(getClass().getSimpleName(), "chooseRoot:" + new Date());
        AsyncDataSourceNode<T> root = chooseRoot(dataSources);

        Log.d(getClass().getSimpleName(), "constructTree:" + new Date());
        constructTree(root, dataSources);

        Log.d(getClass().getSimpleName(), "initModifiedDataAndRemoveInValidNodes:" + new Date());
        initModifiedDataAndRemoveInValidNodes(root);

        Log.d(getClass().getSimpleName(), "getChildrenModifiedData:" + new Date());
        List<T> childrenModifiedData = root.getChildrenModifiedData();

        Log.d(getClass().getSimpleName(), "handleSingles:" + new Date());
        SyncStamp singlesSyncStamp = handleSingles(root, childrenModifiedData, dataSources);

        Log.d(getClass().getSimpleName(), "getAllChildrenNodes:" + new Date());
        List<AsyncDataSourceNode<T>> childrenNodes = getAllChildrenNodes(root);

        childrenNodes.add(root);

        Log.d(getClass().getSimpleName(), "saveAll:" + new Date());
        List<AsyncDataSourceNode<T>> successNodes = saveAll(childrenModifiedData, childrenNodes);

        Log.d(getClass().getSimpleName(), "saveSyncStamps:" + new Date());
        SyncStamp uniteSyncStamp = getUniteSyncStamp(childrenModifiedData, singlesSyncStamp,dataSources);
        saveSyncStamps(successNodes, uniteSyncStamp);

        Log.d(getClass().getSimpleName(), "end:" + new Date());
        return true;
    }

    private SyncStamp getUniteSyncStamp(List<T> childrenModifiedData, SyncStamp singlesSyncStamp, List<MultiDataSource<T>> dataSources) {
        if (childrenModifiedData.isEmpty()){
            if (!dataSources.isEmpty()){
                return singlesSyncStamp;
            }
            return null;
        }
        return SyncStamp.create(
                getLatestLastModifyTime(childrenModifiedData),
                syncTimeStart,
                new Date());
    }

    private SyncStamp handleSingles(AsyncDataSourceNode<T> root, List<T> childrenModifiedData, List<MultiDataSource<T>> dataSources) throws IOException {
        if (dataSources.isEmpty()) {
            return null;
        }
        List<T> rootAll = root.getDataSource().getChangedData(SyncStamp.ZERO, null);
        SyncUtils.blockExecute(executorService
                , new SyncUtils.ParamCallable<MultiDataSource<T>, List<T>>() {
                    @Override
                    public List<T> call(MultiDataSource<T> singleExecutor) throws Exception {
                        return singleExecutor.getChangedData(SyncStamp.ZERO, root.getDataSource());
                    }
                }, new SyncUtils.Handler<MultiDataSource<T>, List<T>>() {
                    @Override
                    public void handle(MultiDataSource<T> param, List<T> result) throws IOException, SaveException {
                        result.removeAll(rootAll);
                        childrenModifiedData.addAll(result);
                        // 保存root的全量
                        param.saveAll(rootAll, root.getDataSource().getKey());
                        // 添加到树
                        AsyncDataSourceNode<T> node = AsyncDataSourceNode.of(param);
                        node.setModifiedData(result);
                        root.addChild(node);

                    }
                }, null, dataSources);
        return SyncStamp.create(
                getLatestLastModifyTime(rootAll),
                syncTimeStart,
                new Date());
    }

    private List<AsyncDataSourceNode<T>> getAllChildrenNodes(AsyncDataSourceNode<T> root) {
        List<AsyncDataSourceNode<T>> nodes = new ArrayList<>();
        root.getAllChildren(nodes);
        return nodes;
    }

    private void saveSyncStamps(List<AsyncDataSourceNode<T>> nodes, SyncStamp syncStamp) {
        if (syncStamp == null){
            return;
        }
        SyncUtils.blockExecute(executorService,
                new SyncUtils.ParamCallable<AsyncDataSourceNode<T>, Void>() {
                    @Override
                    public Void call(AsyncDataSourceNode<T> singleExecutor) throws Exception {
                        MultiDataSource<T> dataSource = singleExecutor.getDataSource();
                        blockExecute(executorServiceExtra,
                                new SyncUtils.ParamCallable<AsyncDataSourceNode<T>, Void>() {
                                    @Override
                                    public Void call(AsyncDataSourceNode<T> successNode) throws Exception {
                                        if (singleExecutor == successNode) {
                                            return null;
                                        }
                                        dataSource.updateLastSyncStamp(syncStamp, successNode.getDataSource());
                                        dataSource.updateLatestSyncStamp(syncStamp);
                                        return null;
                                    }
                                }, null, nodes);

                        return null;
                    }
                }, null, nodes);
    }

    private Date getLatestLastModifyTime(List<T> childrenModifiedData) {
        Date latestTime = null;
        for (T localDatum : childrenModifiedData) {
            if (latestTime == null) {
                latestTime = localDatum.getLastModifyTime();
                continue;
            }
            if (localDatum.getLastModifyTime().compareTo(latestTime) > 0) {
                latestTime = localDatum.getLastModifyTime();
            }
        }

        if (latestTime == null) {
            latestTime = new Date(0);
        }
        return latestTime;
    }

    private List<AsyncDataSourceNode<T>> saveAll(List<T> modifiedData, List<AsyncDataSourceNode<T>> childrenNodes) {
        List<AsyncDataSourceNode<T>> successNodes = new ArrayList<>();
        blockExecute(executorService,
                new SyncUtils.ParamCallable<AsyncDataSourceNode<T>, Void>() {
                    @Override
                    public Void call(AsyncDataSourceNode<T> singleExecutor) throws Exception {
                        singleExecutor.saveData(modifiedData);
//                        context.clearSyncState(
//                                singleExecutor.getDataSource().getKey(),
//                                singleExecutor.getParent().getDataSource().getKey());
                        return null;
                    }
                }, new SyncUtils.Handler<AsyncDataSourceNode<T>, Void>() {
                    @Override
                    public void handle(AsyncDataSourceNode<T> param, Void result) {
                        successNodes.add(param);
                    }
                }, null, childrenNodes);
        return successNodes;
    }

    private void initModifiedDataAndRemoveInValidNodes(AsyncDataSourceNode<T> root) {
        ArrayList<AsyncDataSourceNode<T>> allChildren = new ArrayList<AsyncDataSourceNode<T>>();
        root.getAllChildren(allChildren);
        blockExecute(executorService,
                new SyncUtils.ParamCallable<AsyncDataSourceNode<T>, Void>() {
                    public Void call(AsyncDataSourceNode<T> singleExecutor) throws Exception {
                        singleExecutor.initModifiedData();
                        return null;
                    }
                }, new SyncUtils.ParamCallable<AsyncDataSourceNode<T>, Void>() {
                    public Void call(AsyncDataSourceNode<T> singleExecutor) throws Exception {
                        // 把子节点从树中移除
                        singleExecutor.getParent().getChildren().remove(singleExecutor);
                        singleExecutor.setParent(null);
                        return null;
                    }
                }, allChildren);
    }

    private void initSyncStamps(List<MultiDataSource<T>> list) {
        for (MultiDataSource<T> dataSource : list) {
            dataSource.setChosenKey(null);
            dataSource.setSyncStampsCache(new LinkedHashMap<>());
        }
        blockExecute(executorService,
                new SyncUtils.ParamCallable<MultiDataSource<T>, Void>() {
                    public Void call(MultiDataSource<T> singleExecutor) throws Exception {
                        singleExecutor.initLastSyncStamps();
                        return null;
                    }
                }, new SyncUtils.ParamCallable<MultiDataSource<T>, Void>() {
                    public Void call(MultiDataSource<T> singleExecutor) throws Exception {
                        System.out.println(singleExecutor);
                        return null;
                    }
                }, list);
    }

    private AsyncDataSourceNode<T> chooseRoot(List<MultiDataSource<T>> dataSources) {
        initSyncKeyForSync(dataSources);
        MultiDataSource<T> rootDataSource = getMaxConnectedDataSource(dataSources);
        AsyncDataSourceNode<T> rootNode = AsyncDataSourceNode.of(rootDataSource);
        dataSources.remove(rootDataSource);
        return rootNode;
    }

    private void initSyncKeyForSync(List<MultiDataSource<T>> list) {
        for (MultiDataSource<T> dataSource : list) {
            chooseInternal(list, dataSource);
        }
    }

    private void chooseInternal(List<MultiDataSource<T>> list, MultiDataSource<T> dataSource) {
        Map<String, SyncStamp> syncStamps = dataSource.getSyncStampsCache();
        for (String key : syncStamps.keySet()) {
            boolean found = false;
            for (MultiDataSource<T> ds : list) {
                if (ds.getKey().equals(key)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                dataSource.setChosenKey(key);
                break;
            }
        }
    }

    private MultiDataSource<T> getMaxConnectedDataSource(List<MultiDataSource<T>> list) {
        MultiDataSource<T> result = list.get(0);
        int resultCount = getConnectedDataSourceCount(result, list);
        for (MultiDataSource<T> dataSource : list) {
            int count = getConnectedDataSourceCount(dataSource, list);
            if (count > resultCount) {
                result = dataSource;
                resultCount = count;
            }
        }
        return result;
    }

    private int getConnectedDataSourceCount(MultiDataSource<T> dataSource, List<MultiDataSource<T>> list) {
        int count = 0;
        for (MultiDataSource<T> ds : list) {
            if (dataSource.getKey().equals(ds.getChosenKey())) {
                count++;
            }
        }
        return count;
    }

    private void constructTree(AsyncDataSourceNode<T> root, List<MultiDataSource<T>> dataSources) {
        if (dataSources == null || dataSources.isEmpty()) {
            return;
        }
        Iterator<MultiDataSource<T>> iterator = dataSources.iterator();
        while (iterator.hasNext()) {
            MultiDataSource<T> next = iterator.next();
            if (root.getDataSource().getKey().equals(next.getChosenKey())) {
                root.addChild(AsyncDataSourceNode.of(next));
                iterator.remove();
            }
        }
        for (AsyncDataSourceNode<T> child : root.getChildren()) {
            constructTree(child, dataSources);
        }
    }

}
