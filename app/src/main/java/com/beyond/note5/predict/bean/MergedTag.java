package com.beyond.note5.predict.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.beyond.note5.predict.utils.TagUtils;

import java.util.List;

/**
 * @author beyondlov1
 * @date 2019/03/15
 */
public class MergedTag extends Tag{

    @JSONField(serialize = false)
    private List<Tag> children;
    private List<Integer> childrenIndexes;

    private MergedTag(){

    }

    public static MergedTag create(List<Tag> children){
        MergedTag mergedTag = new MergedTag();
        if (children.size()<1){
            throw new RuntimeException("mergedTag can not be empty");
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Tag tag : children) {
            stringBuilder.append(tag.getContent());
        }
        mergedTag.setId(TagUtils.uuid());
        mergedTag.setName(stringBuilder.toString());
        mergedTag.setContent(stringBuilder.toString());
        mergedTag.setChildren(children);
        return mergedTag;
    }

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
