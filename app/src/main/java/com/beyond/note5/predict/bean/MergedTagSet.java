package com.beyond.note5.predict.bean;

import com.beyond.note5.predict.utils.TagUtils;

import java.util.List;

/**
 * @author beyondlov1
 * @date 2019/03/15
 */
public class MergedTagSet {  //TODO: 继承Tag
    private Tag startTag;
    private List<Tag> tags;
    private Tag mergedTag;
    private int count;

    public MergedTagSet(List<Tag> tags){
        if (tags.size()<1){
            throw new RuntimeException("mergedTag can not be empty");
        }
        StringBuilder stringBuilder = new StringBuilder();
        this.tags = tags;
        for (Tag tag : tags) {
            stringBuilder.append(tag.getContent());
        }
        this.mergedTag = TagUtils.createTag(stringBuilder.toString());

        this.startTag = tags.get(0);
        this.count = tags.size();
    }


    public boolean contains(Tag tag){
        return tags.contains(tag);
    }

    public Tag getStartTag() {
        return startTag;
    }

    public void setStartTag(Tag startTag) {
        this.startTag = startTag;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public Tag getMergedTag() {
        return mergedTag;
    }

    public void setMergedTag(Tag mergedTag) {
        this.mergedTag = mergedTag;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
