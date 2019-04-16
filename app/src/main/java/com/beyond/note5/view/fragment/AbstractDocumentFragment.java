package com.beyond.note5.view.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.beyond.note5.bean.Document;
import com.beyond.note5.view.adapter.AbstractDocumentViewFragment;
import com.beyond.note5.view.adapter.component.DocumentRecyclerViewAdapter;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author: beyond
 * @date: 2019/1/31
 */
@SuppressWarnings("unchecked")
public abstract class AbstractDocumentFragment<T extends Document> extends AbstractDocumentViewFragment<T> {

    protected RecyclerView recyclerView;

    protected DocumentRecyclerViewAdapter recyclerViewAdapter;

    protected List<T> data = new ArrayList<>();
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onAddSuccess(T t) {
        int insertIndex = recyclerViewAdapter.getItemDataGenerator().getInsertIndex(t);
        data.add(insertIndex, t);
        recyclerViewAdapter.notifyInserted(t);
        recyclerView.scrollToPosition(insertIndex);
        msg("添加成功");
    }

    @Override
    public void onDeleteSuccess(T t) {
        int index = recyclerViewAdapter.getItemDataGenerator().getIndexById(t);
        if (index!=-1){
            data.remove(index);
            recyclerViewAdapter.notifyRemoved(t);
            msg("删除成功");
        }
    }

    @Override
    public void onUpdateSuccess(T t) {
        Iterator<T> iterator = data.iterator();
        while (iterator.hasNext()) {
            T oldT = iterator.next();
            if (StringUtils.equals(oldT.getId(), t.getId())) {
                iterator.remove();
                recyclerViewAdapter.notifyRemoved(oldT);
                int insertIndex = recyclerViewAdapter.getItemDataGenerator().getInsertIndex(t);
                data.add(insertIndex, t);
                recyclerViewAdapter.notifyInserted(t);
                recyclerView.scrollToPosition(insertIndex);
                msg("更新成功");
                break;
            }
        }
    }

    @Override
    public void onUpdatePrioritySuccess(T t) {
        for (T oldT : data) {
            if (StringUtils.equals(oldT.getId(), t.getId())) {
                recyclerViewAdapter.notifyChanged(t);
                break;
            }
        }
    }

    @Override
    public void onFindAllSuccess(List<T> allT) {
        data.clear();
        recyclerViewAdapter.notifyFullRangeRemoved();
        data.addAll(allT);
        recyclerViewAdapter.notifyFullRangeInserted();
    }

    public DocumentRecyclerViewAdapter getRecyclerViewAdapter() {
        return recyclerViewAdapter;
    }
}
