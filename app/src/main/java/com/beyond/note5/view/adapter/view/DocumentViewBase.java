package com.beyond.note5.view.adapter.view;

import android.support.v7.widget.RecyclerView;

import com.beyond.note5.bean.Document;
import com.beyond.note5.view.DocumentView;
import com.beyond.note5.view.adapter.component.DocumentRecyclerViewAdapter;

import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unchecked")
public abstract class DocumentViewBase<T extends Document> implements DocumentView<T> {
    @Override
    public void onAddSuccess(T t) {
        int insertIndex = getRecyclerViewAdapter().getItemDataGenerator().getInsertIndex(t);
        getData().add(insertIndex, t);
        getRecyclerViewAdapter().notifyInserted(t);
        if (getRecyclerView() != null){
            getRecyclerView().scrollToPosition(insertIndex);
        }
    }

    @Override
    public void onAddFail(T document) {

    }

    @Override
    public void onDeleteSuccess(T t) {
        int index = getRecyclerViewAdapter().getItemDataGenerator().getIndexById(t);
        if (index!=-1){
            getData().remove(index);
            getRecyclerViewAdapter().notifyRemoved(t);
        }
    }

    @Override
    public void onUpdateFail(T document) {

    }

    @Override
    public void onUpdateSuccess(T t) {
        Iterator<T> iterator = getData().iterator();
        while (iterator.hasNext()) {
            T oldT = iterator.next();
            if (StringUtils.equals(oldT.getId(), t.getId())) {
                iterator.remove();
                getRecyclerViewAdapter().notifyRemoved(oldT);
                int insertIndex = getRecyclerViewAdapter().getItemDataGenerator().getInsertIndex(t);
                getData().add(insertIndex, t);
                getRecyclerViewAdapter().notifyInserted(t);
                if (getRecyclerView() != null) {
                    getRecyclerView().scrollToPosition(insertIndex);
                }
                break;
            }
        }
    }

    @Override
    public void onUpdatePrioritySuccess(T t) {
        for (T oldT : getData()) {
            if (StringUtils.equals(oldT.getId(), t.getId())) {
                getRecyclerViewAdapter().notifyChanged(t);
                break;
            }
        }
    }

    @Override
    public void onUpdatePriorityFail(T document) {

    }

    @Override
    public void onFindAllSuccess(List<T> allT) {
        getData().clear();
        getRecyclerViewAdapter().notifyFullRangeRemoved();
        getData().addAll(allT);
        getRecyclerViewAdapter().notifyFullRangeInserted();
    }

    @Override
    public void onFindAllFail() {

    }

    @Override
    public void onDeleteFail(T document) {

    }

    @Override
    public void onAddAllSuccess(List<T> addList) {
    }

    @Override
    public void onAddAllFail(Exception e) {
        getRecyclerViewAdapter().notifyDataSetChanged();
    }

    @Override
    public void onUpdateAllSuccess(List<T> updateList) {
    }

    @Override
    public void onUpdateAllFail(Exception e) {
        getRecyclerViewAdapter().notifyDataSetChanged();
    }

    public abstract DocumentRecyclerViewAdapter getRecyclerViewAdapter();

    public abstract RecyclerView getRecyclerView() ;

    public abstract List<T> getData() ;
}
