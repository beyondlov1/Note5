package com.beyond.note5.sync;

import com.beyond.note5.sync.datasource.MultiDataSource;
import com.beyond.note5.sync.datasource.entity.SyncStamp;
import com.beyond.note5.sync.exception.SaveException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AsyncDataSourceNode<T> {

    private AsyncDataSourceNode<T> parent;
    private List<AsyncDataSourceNode<T>> children = new ArrayList<>();
    private MultiDataSource<T> dataSource;
    private List<T> modifiedData = new ArrayList<T>();


    public static <T> AsyncDataSourceNode<T> of(MultiDataSource<T> dataSource) {
        AsyncDataSourceNode<T> node = new AsyncDataSourceNode<T>();
        node.setDataSource(dataSource);
        return node;
    }

    public void getAllChildren(List<AsyncDataSourceNode<T>> result) {
        for (AsyncDataSourceNode<T> child : children) {
            result.add(child);
            child.getAllChildren(result);
        }
    }

    public List<T> getChildrenModifiedData() {
        List<T> childrenModifiedData = new ArrayList<T>();
        for (AsyncDataSourceNode<T> child : children) {
            childrenModifiedData.addAll(child.modifiedData);
            childrenModifiedData.addAll(child.getChildrenModifiedData());
        }
        return childrenModifiedData;
    }

    public void initModifiedData() throws IOException {
        MultiDataSource<T> parentDataSource = parent.getDataSource();
        SyncStamp lastSyncStamp = this.dataSource.getLastSyncStamp(parentDataSource);
        if (this.dataSource.isChanged(parentDataSource)){
            modifiedData = this.dataSource.getChangedData(lastSyncStamp,parentDataSource);
        }
    }

    public void saveData(List<T> data) throws IOException, SaveException {
        Set<T> notMineData = new HashSet<>(data);
        notMineData.removeAll(modifiedData);
        if (notMineData.isEmpty()){
            return;
        }

        if (getParent() != null){
            dataSource.saveAll(new ArrayList<>(notMineData), getParent().getDataSource().getKey());
        }else {
            // FIXME: 这里会影响 State 的保存, 暂时还不知道用谁的key比较好
            dataSource.saveAll(new ArrayList<>(notMineData), null);
        }
    }

    public AsyncDataSourceNode<T> addChild(AsyncDataSourceNode<T> node) {
        node.setParent(this);
        children.add(node);
        return this;
    }



    public List<AsyncDataSourceNode<T>> getChildren() {
        return children;
    }

    public void setChildren(List<AsyncDataSourceNode<T>> children) {
        this.children = children;
        for (AsyncDataSourceNode<T> child : children) {
            child.setParent(this);
        }
    }

    public MultiDataSource<T> getDataSource() {
        return dataSource;
    }

    public void setDataSource(MultiDataSource<T> dataSource) {
        this.dataSource = dataSource;
    }

    public AsyncDataSourceNode<T> getParent() {
        return parent;
    }

    public void setParent(AsyncDataSourceNode<T> parent) {
        this.parent = parent;
    }

    public List<T> getModifiedData() {
        return modifiedData;
    }

    public void setModifiedData(List<T> modifiedData) {
        this.modifiedData = modifiedData;
    }
}
