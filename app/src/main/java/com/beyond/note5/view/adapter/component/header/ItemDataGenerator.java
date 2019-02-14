package com.beyond.note5.view.adapter.component.header;

import com.beyond.note5.bean.Element;

import java.util.List;

public interface ItemDataGenerator<T> {

    void refresh();

    List<Element> getItemData();

    List<T> getContentData();

    List<Header> getHeaderData();

    int getPosition(T t);

    int getIndex(T t);

    int getInsertIndex(T t);

    List<Integer> getSingleContentPositions();
}
