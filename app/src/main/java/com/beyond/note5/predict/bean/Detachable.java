package com.beyond.note5.predict.bean;

import java.util.List;

public interface Detachable<T> {

    List<T> getChildren();

    void setChildren(List<T> children);

    List<Integer> getChildrenIndexes() ;

    void setChildrenIndexes(List<Integer> childrenIndexes);
}
