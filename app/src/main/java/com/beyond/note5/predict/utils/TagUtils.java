package com.beyond.note5.predict.utils;

import com.beyond.note5.predict.bean.MergedTag;
import com.beyond.note5.predict.bean.MergedTimeTag;
import com.beyond.note5.predict.bean.Tag;
import com.beyond.note5.predict.bean.TagEdge;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.greendao.annotation.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author beyondlov1
 * @date 2019/03/10
 */
public class TagUtils {
    public static String uuid(){
        return  UUID.randomUUID().toString().replace("-","");
    }

    /**
     * Unicode转 汉字字符串
     *
     * @param str \u6728
     * @return '木' 26408
     */
    public static String unicodeToString(String str) {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(str);
        char ch;
        while (matcher.find()) {
            //group 6728
            String group = matcher.group(2);
            //ch:'木' 26408
            ch = (char) Integer.parseInt(group, 16);
            //group1 \u6728
            String group1 = matcher.group(1);
            str = str.replace(group1, ch + "");
        }
        return str;
    }

    public static Tag findTagByContent(List<Tag> tags, String content){
        Tag foundTag = null;
        for (Tag root : tags) {
            if (StringUtils.equals(root.getContent(),content)){
                foundTag = root;
                break;
            }
        }
        return foundTag;
    }

    public static TagEdge findTagEdgeByContent(List<TagEdge> tagEdges, String content){
        TagEdge found = null;
        for (TagEdge edge : tagEdges) {
            if (StringUtils.equals(edge.getTag().getContent(),content)){
                found = edge;
                break;
            }
        }
        return found;
    }

    public static Tag createTag(String content){
        if (StringUtils.isNoneBlank(content)){
            Tag tag = new Tag();
            tag.setId(uuid());
            tag.setName(content);
            tag.setContent(content);
            return tag;
        }else {
            throw new RuntimeException("content can not be null");
        }
    }
    @Deprecated
    public static TagEdge createTagEdge(Tag tag){
        TagEdge tagEdge = new TagEdge();
        tagEdge.setId(uuid());
        tagEdge.setTag(tag);
        return tagEdge;
    }

    public static TagEdge createTagEdge(Tag tag,@NotNull List<Tag> rootTags){
        TagEdge tagEdge = new TagEdge();
        tagEdge.setId(uuid());
        tagEdge.setTag(tag);
        tagEdge.setIndex(rootTags.indexOf(tag));
        return tagEdge;
    }


    @Deprecated
    public static MergedTag createMergedTag(List<Tag> children){
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
        mergedTag.setFirst(children.get(0).isFirst());
        mergedTag.setChildren(children);
        return mergedTag;
    }

    public static MergedTag createMergedTag(List<Tag> children,List<Tag> rootTags){
        MergedTag mergedTag = createMergedTag(children);
        List<Integer> childrenIndexes = new ArrayList<>();
        for (Tag child : children) {
            childrenIndexes.add(rootTags.indexOf(child));
        }
        mergedTag.setChildrenIndexes(childrenIndexes);
        return mergedTag;
    }

    public static Tag addScore(Tag tag, int i){
        tag.setScore(tag.getScore()+i);
        return tag;
    }

    public static TagEdge addScore(TagEdge tagEdge, int i){
        tagEdge.setScore(tagEdge.getScore()+i);
        return tagEdge;
    }

    public static void copyTagTo(Tag source, Tag target){
        target.setId(source.getId());
        target.setName(source.getName());
        target.setContent(source.getContent());
        target.setScore(source.getScore());
        target.setFirst(source.isFirst());
        target.setEdges(source.getEdges());
    }

    public static void copyMergedTagTo(MergedTag source, MergedTimeTag target){
        copyTagTo(source,target);
        target.setChildren(source.getChildren());
        target.setChildrenIndexes(source.getChildrenIndexes());
    }

}
