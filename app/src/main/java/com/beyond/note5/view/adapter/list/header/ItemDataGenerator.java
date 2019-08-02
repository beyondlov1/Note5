package com.beyond.note5.view.adapter.list.header;

import com.beyond.note5.bean.Element;

import java.util.List;

public interface ItemDataGenerator<T,S> {

    void refresh();

    List<Element> getItemData();

    List<T> getContentData();

    List<S> getHeaderData();

    int getPosition(T t);

    int getIndex(T t);

    int getIndexById(T t);

    int getInsertIndex(T t);

    List<Integer> getSingleContentPositions();
}
