package com.beyond.note5.predict.bean;

import com.beyond.note5.predict.utils.TagUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author beyondlov1
 * @date 2019/03/10
 */
public class TagGraph {
    private String id;
    private List<Tag> tags = new ArrayList<Tag>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public void addTag(Tag tag){
        tags.add(tag);
    }

    public Tag find(String str){
        return TagUtils.findTagByContent(tags,str);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Tag tag : tags) {
            stringBuilder.append(tag.getName()).append("-").append(tag.getScore()).append("/");
            List<TagEdge> edges = tag.getEdges();
            for (TagEdge edge : edges) {
                if (edge.getTag()!=null){
                    stringBuilder.append(edge.getTag().getName()).append("-").append(edge.getScore()).append(" ");
                }
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public List<Tag> predict(String source){
        List<Tag> result = new ArrayList<Tag>();
        for (Tag tag : tags) {
            if (source.endsWith(tag.getContent())){
                for (TagEdge edge : tag.getEdges()) {
                    result.add(edge.getTag());
                }
            }
        }
        return result;
    }
}
