package com.beyond.note5.predict.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.LinkedList;
import java.util.List;

/**
 * @author beyondlov1
 * @date 2019/03/10
 */
public class Tag {
    @JSONField(serialize = false)
    private String id;
    @JSONField(serialize = false)
    private String name;
    private String content;
    private int score;
    private List<TagEdge> edges = new LinkedList<TagEdge>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<TagEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<TagEdge> edges) {
        this.edges = edges;
    }

    public TagEdge findEdge(Tag tag) {
        for (TagEdge edge : edges) {
            if (edge.getTag()!=null){
                if (edge.getTag().getContent().equals(tag.getContent())){
                    return edge;
                }
            }
        }
        return null;
    }
}
