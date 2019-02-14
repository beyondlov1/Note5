package com.beyond.note5.view.adapter.component.header;

import com.beyond.note5.bean.Element;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractItemDataGenerator<T> implements ItemDataGenerator<T> {

    protected List<T> contentData;
    protected List<Element> itemData;
    protected List<Header> headerData;
    private List<Integer> singleContentPosition;


    public AbstractItemDataGenerator(List<T> contentData) {
        this.contentData = contentData;
        this.itemData = new ArrayList<>();
        this.headerData = new ArrayList<>();
        this.singleContentPosition = new ArrayList<>();
        refresh();
    }

    protected abstract void init();

    @Override
    public void refresh() {
        this.itemData.clear();
        this.headerData.clear();
        this.singleContentPosition.clear();
        init();
        initSingleDataPosition();
    }

    private void initSingleDataPosition() {
        Integer lastPosition = null;
        for (Header header : headerData) {
            int position = header.getPosition();
            if (lastPosition != null){
                if (position - lastPosition == 2){
                    singleContentPosition.add(position-1);
                }
            }
            lastPosition = position;
        }

        if (lastPosition == null){
            return;
        }

        if (lastPosition == itemData.size()-2){
            singleContentPosition.add(itemData.size()-1);
        }

    }

    @Override
    public List<Element> getItemData() {
        return itemData;
    }

    @Override
    public List<T> getContentData() {
        return contentData;
    }

    @Override
    public List<Header> getHeaderData() {
        return headerData;
    }

    @Override
    public int getPosition(T t) {
        return itemData.indexOf(t);
    }

    @Override
    public int getIndex(T t) {
        return contentData.indexOf(t);
    }

    @Override
    public List<Integer> getSingleContentPositions() {

        return singleContentPosition;
    }
}
