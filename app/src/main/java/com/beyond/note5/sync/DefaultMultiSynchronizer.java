package com.beyond.note5.sync;

import android.util.Log;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Tracable;
import com.beyond.note5.service.SyncRetryService;
import com.beyond.note5.sync.context.entity.SyncState;
import com.beyond.note5.sync.context.model.SyncStateModel;
import com.beyond.note5.sync.context.model.SyncStateModelImpl;
import com.beyond.note5.sync.datasource.MultiDataSource;
import com.beyond.note5.sync.datasource.entity.SyncStamp;
import com.beyond.note5.sync.exception.MessageException;
import com.beyond.note5.sync.exception.SaveException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.beyond.note5.MyApplication.CPU_COUNT;
import static com.beyond.note5.sync.SyncUtils.blockExecute;

/**
 * @author: beyond
 * @date: 2019/8/9
 */
//TODO: STATE
public class DefaultMultiSynchronizer<T extends Tracable> implements Synchronizer<T> {

    private long remoteLockTimeOutMills = 30 * 60000L; // 30min
    private Lock lock;

    private List<MultiDataSource<T>> dataSources;

    private ExecutorService executorService;

    private String[] dataSourceKeys;

    private ExecutorService executorServiceExtra;  // 防止多线程嵌套时死锁

    private final SyncStateModel syncStateModel;

    private Date syncTimeStart;

    public DefaultMultiSynchronizer(List<MultiDataSource<T>> dataSources, ExecutorService executorService) {
        this.dataSources = dataSources;
        this.executorService = executorService;
        this.executorServiceExtra = new ThreadPoolExecutor(
                CPU_COUNT*2+1, 60,
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
        this.lock = new ReentrantLock();
        ArrayList<String> keyList = new ArrayList<>(dataSources.size());
        for (MultiDataSource<T> dataSource : dataSources) {
            keyList.add(dataSource.getKey());
        }
        this.dataSourceKeys = keyList.toArray(new String[0]);
        this.syncStateModel = new SyncStateModelImpl();
    }

    @Override
    public boolean sync() throws Exception {
        if (lock.tryLock()) {
            try {
                Log.d(getClass().getSimpleName(), "同步开始");
                doSync();
                Log.d(getClass().getSimpleName(), "同步成功");
            } catch (Exception e) {
                long delay = (new Random().nextInt(20) + 35) * 60 * 1000;
                retryIfNecessary(delay);
                Log.e(getClass().getSimpleName(), "同步失败", e);
                e.printStackTrace();
                throw new MessageException(e);
            } finally {
                lock.unlock();
            }
        } else {
            Log.d(getClass().getSimpleName(), "同步正在进行, 本次同步取消");
        }
        return true;
    }

    private void retryIfNecessary(long delay) {
        SyncRetryService.retryIfNecessary(MyApplication.getInstance(), delay);
        SyncRetryService.addFailCount();
    }

    private void doSync() throws Exception {

        if (!remoteLock()){
            releaseLock();
            return;
        }

        syncTimeStart = new Date();

        List<MultiDataSource<T>> dataSources = new ArrayList<>(this.dataSources);

        Log.d(getClass().getSimpleName(), "initSyncStamps:" + new Date());
        initSyncStamps(dataSources);

        Log.d(getClass().getSimpleName(), "chooseRoot:" + new Date());
        MultiDataSourceNode<T> root = chooseRoot(dataSources);

        Log.d(getClass().getSimpleName(), "constructTree:" + new Date());
        constructTree(root, dataSources);

        Log.d(getClass().getSimpleName(), "initModifiedDataAndRemoveInValidNodes:" + new Date());
        initModifiedDataAndRemoveInValidNodes(root);

        Log.d(getClass().getSimpleName(), "getChildrenModifiedData:" + new Date());
        List<T> childrenModifiedData = root.getChildrenModifiedData();

        Log.d(getClass().getSimpleName(), "handleSingles:" + new Date());
        SyncStamp singlesSyncStamp = handleSingles(root, childrenModifiedData, dataSources);

        Log.d(getClass().getSimpleName(), "getAllChildrenNodes:" + new Date());
        List<MultiDataSourceNode<T>> childrenNodes = getAllChildrenNodes(root);

        childrenNodes.add(root);

        Log.d(getClass().getSimpleName(), "saveAll:" + new Date());
        List<MultiDataSourceNode<T>> successNodes = saveAll(childrenModifiedData, childrenNodes);

        Log.d(getClass().getSimpleName(), "saveSyncStamps:" + new Date());
        SyncStamp uniteSyncStamp = getUniteSyncStamp(childrenModifiedData, singlesSyncStamp,dataSources);
        saveSyncStamps(successNodes, uniteSyncStamp);

        List<SyncState> beforeClear = syncStateModel.findAll(null);
        System.out.println(beforeClear);

        Log.d(getClass().getSimpleName(), "clearSyncState:" + new Date());
        clearSyncState(successNodes, root);

        List<SyncState> afterClear = syncStateModel.findAll(null);
        System.out.println(afterClear);

        Log.d(getClass().getSimpleName(), "end:" + new Date());

        releaseLock();
    }

    private void clearSyncState(List<MultiDataSourceNode<T>> successNodes,MultiDataSourceNode<T> root) {

        if (dataSources == null || dataSources.isEmpty()){
            return;
        }

        List<MultiDataSource<T>> successDataSources = extract(successNodes);

        ArrayList<MultiDataSourceNode<T>> allConnectedNodes = new ArrayList<>();
        root.getAllChildren(allConnectedNodes);
        List<MultiDataSource<T>> allConnectedDataSource = extract(allConnectedNodes);

        ArrayList<MultiDataSource<T>> inValidDataSource = new ArrayList<>(dataSources);
        inValidDataSource.removeAll(allConnectedDataSource);

        List<MultiDataSource<T>> nonRecordDataSource = new ArrayList<>(successDataSources);
        nonRecordDataSource.addAll(inValidDataSource);

        List<String> keys = new ArrayList<>(nonRecordDataSource.size());
        for (MultiDataSource<T> dataSource : nonRecordDataSource) {
            keys.add(dataSource.getKey());
        }

        syncStateModel.deleteConnectedEachOtherIn(keys,dataSources.get(0).clazz());
    }

    private List<MultiDataSource<T>>  extract(List<MultiDataSourceNode<T>> nodes){
        List<MultiDataSource<T>> dataSources = new ArrayList<>(nodes.size());
        for (MultiDataSourceNode<T> node : nodes) {
            dataSources.add(node.getDataSource());
        }
        return dataSources;
    }

    private void releaseLock() {
        blockExecute(executorService,
                new SyncUtils.ParamCallable<MultiDataSource<T>, Void>() {
                    @Override
                    public Void call(MultiDataSource<T> singleExecutor) throws Exception {
                        singleExecutor.release();
                        return null;
                    }
                },null,
                dataSources);
    }

    private boolean remoteLock() {
        final boolean[] locked = {true};
        blockExecute(executorService,
                new SyncUtils.ParamCallable<MultiDataSource<T>, Boolean>() {
                    @Override
                    public Boolean call(MultiDataSource<T> singleExecutor) throws Exception {
                        return singleExecutor.tryLock(remoteLockTimeOutMills);
                    }
                }, new SyncUtils.Handler<MultiDataSource<T>, Boolean>() {
                    @Override
                    public void handle(MultiDataSource<T> param, Boolean result) throws IOException, SaveException {
                        locked[0] = result && locked[0];
                    }
                },null,
                dataSources);
        return locked[0];
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

    private SyncStamp handleSingles(MultiDataSourceNode<T> root, List<T> childrenModifiedData, List<MultiDataSource<T>> dataSources) throws IOException {
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
                        param.saveAll(rootAll,dataSourceKeys);
                        // 添加到树
                        MultiDataSourceNode<T> node = MultiDataSourceNode.of(param);
                        node.setModifiedData(result);
                        root.addChild(node);

                    }
                }, null, dataSources);
        return SyncStamp.create(
                getLatestLastModifyTime(rootAll),
                syncTimeStart,
                new Date());
    }

    private List<MultiDataSourceNode<T>> getAllChildrenNodes(MultiDataSourceNode<T> root) {
        List<MultiDataSourceNode<T>> nodes = new ArrayList<>();
        root.getAllChildren(nodes);
        return nodes;
    }

    private void saveSyncStamps(List<MultiDataSourceNode<T>> nodes, SyncStamp syncStamp) {
        if (syncStamp == null){
            return;
        }
        SyncUtils.blockExecute(executorService,
                new SyncUtils.ParamCallable<MultiDataSourceNode<T>, Void>() {
                    @Override
                    public Void call(MultiDataSourceNode<T> singleExecutor) throws Exception {
                        MultiDataSource<T> dataSource = singleExecutor.getDataSource();
                        blockExecute(executorServiceExtra,
                                new SyncUtils.ParamCallable<MultiDataSourceNode<T>, Void>() {
                                    @Override
                                    public Void call(MultiDataSourceNode<T> successNode) throws Exception {
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

    private List<MultiDataSourceNode<T>> saveAll(List<T> modifiedData, List<MultiDataSourceNode<T>> childrenNodes) {
        List<MultiDataSourceNode<T>> successNodes = new ArrayList<>();
        blockExecute(executorService,
                new SyncUtils.ParamCallable<MultiDataSourceNode<T>, Void>() {
                    @Override
                    public Void call(MultiDataSourceNode<T> singleExecutor) throws Exception {
                        singleExecutor.saveData(modifiedData,dataSourceKeys);
                        return null;
                    }
                }, new SyncUtils.Handler<MultiDataSourceNode<T>, Void>() {
                    @Override
                    public void handle(MultiDataSourceNode<T> param, Void result) {
                        successNodes.add(param);
                    }
                }, null, childrenNodes);
        return successNodes;
    }

    private void initModifiedDataAndRemoveInValidNodes(MultiDataSourceNode<T> root) {
        ArrayList<MultiDataSourceNode<T>> allChildren = new ArrayList<MultiDataSourceNode<T>>();
        root.getAllChildren(allChildren);
        blockExecute(executorService,
                new SyncUtils.ParamCallable<MultiDataSourceNode<T>, Void>() {
                    public Void call(MultiDataSourceNode<T> singleExecutor) throws Exception {
                        singleExecutor.initModifiedData();
                        return null;
                    }
                }, new SyncUtils.ParamCallable<MultiDataSourceNode<T>, Void>() {
                    public Void call(MultiDataSourceNode<T> singleExecutor) throws Exception {
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

    private MultiDataSourceNode<T> chooseRoot(List<MultiDataSource<T>> dataSources) {
        initSyncKeyForSync(dataSources);
        MultiDataSource<T> rootDataSource = getMaxConnectedDataSource(dataSources);
        MultiDataSourceNode<T> rootNode = MultiDataSourceNode.of(rootDataSource);
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

    private void constructTree(MultiDataSourceNode<T> root, List<MultiDataSource<T>> dataSources) {
        if (dataSources == null || dataSources.isEmpty()) {
            return;
        }
        Iterator<MultiDataSource<T>> iterator = dataSources.iterator();
        while (iterator.hasNext()) {
            MultiDataSource<T> next = iterator.next();
            if (root.getDataSource().getKey().equals(next.getChosenKey())) {
                root.addChild(MultiDataSourceNode.of(next));
                iterator.remove();
            }
        }
        for (MultiDataSourceNode<T> child : root.getChildren()) {
            constructTree(child, dataSources);
        }
    }

}
