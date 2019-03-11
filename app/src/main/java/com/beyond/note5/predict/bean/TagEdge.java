package com.beyond.note5.predict.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author beyondlov1
 * @date 2019/03/10
 */
public class TagEdge {
    @JSONField(serialize = false)
    private String id;
    private int index;
    @JSONField(serialize=false)
    private Tag tag;
    private int score;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
