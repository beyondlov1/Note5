package com.beyond.note5.predict.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * @author beyondlov1
 * @date 2019/03/15
 */
public class MergedTag extends Tag implements Detachable<Tag>{

    @JSONField(serialize = false)
    private List<Tag> children;
    private List<Integer> childrenIndexes;

    public List<Tag> getChildren() {
        return children;
    }

    public void setChildren(List<Tag> children) {
        this.children = children;
    }

    public List<Integer> getChildrenIndexes() {
        return childrenIndexes;
    }

    public void setChildrenIndexes(List<Integer> childrenIndexes) {
        this.childrenIndexes = childrenIndexes;
    }
}
