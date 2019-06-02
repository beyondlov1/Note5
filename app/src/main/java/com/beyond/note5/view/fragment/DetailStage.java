package com.beyond.note5.view.fragment;

import android.view.View;

import com.beyond.note5.constant.LoadType;

import java.util.List;

public interface DetailStage<T> {
    void setData(List<T> data);
    void setCurrentIndex(int currentIndex);
    void setEnterIndex(int enterIndex);
    List<T> getData();
    int getCurrentIndex();
    int getEnterIndex();
    void prev();
    void next();
    void refresh();
    void setOnViewMadeListener(OnViewMadeListener listener);
    void setLoadType(LoadType loadType);
    void loadMore();

    interface OnViewMadeListener{
        void onViewMade(View view);
    }
}
