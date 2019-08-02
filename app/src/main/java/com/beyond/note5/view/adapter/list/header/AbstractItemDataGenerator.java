package com.beyond.note5.view.adapter.list.header;

import com.beyond.note5.bean.Element;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractItemDataGenerator<T extends Element,S extends Header> implements ItemDataGenerator<T,S> {

    List<T> contentData; //content
    List<S> headerData; //header
    List<Element> itemData;  // header+content
    private List<Integer> singleContentPosition; // single content for full span


    AbstractItemDataGenerator(List<T> contentData) {
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
    public List<S> getHeaderData() {
        return headerData;
    }

    @Override
    public int getPosition(T t) {
        return getPositionById(t);
    }

    private int getPositionById(T t){
        int index = 0;
        for (Element itemDatum : itemData) {
            if (StringUtils.equals(itemDatum.getId(),t.getId())){
                return index;
            }
            index++;
        }
        return -1;
    }

    @Override
    public int getIndex(T t) {
        return getIndexById(t);
    }

    @Override
    public int getIndexById(T t) {
        int index = 0;
        for (T contentDatum : contentData) {
            if (StringUtils.equals(contentDatum.getId(),t.getId())){
                return index;
            }
            index++;
        }
        return -1;
    }

    @Override
    public List<Integer> getSingleContentPositions() {

        return singleContentPosition;
    }
}
