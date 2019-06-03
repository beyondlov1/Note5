package com.beyond.note5.view.fragment;

import android.view.View;

import com.beyond.note5.view.listener.OnBackPressListener;

import java.util.List;

public interface MultiDetailStage<T> extends DetailStage, OnBackPressListener {
    void setData(List<T> data);
    void setCurrentIndex(int currentIndex);
    void setEnterIndex(int enterIndex);
    List<T> getData();
    T getCurrentData();
    int getCurrentIndex();
    int getEnterIndex();
    void prev();
    void next();
    void setViewFactory(ViewFactory viewFactory);

    interface ViewFactory<V extends View>{
        V getView();
    }
}
