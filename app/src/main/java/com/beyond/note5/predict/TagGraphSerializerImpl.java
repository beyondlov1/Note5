package com.beyond.note5.predict;

import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.beyond.note5.predict.bean.MergedTag;
import com.beyond.note5.predict.bean.Tag;
import com.beyond.note5.predict.bean.TagEdge;
import com.beyond.note5.predict.bean.TagGraph;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author beyondlov1
 * @date 2019/03/10
 */
public class TagGraphSerializerImpl extends Observable implements TagGraphSerializer {

    private TagGraph tagGraph;
    private File file;
    private AtomicBoolean running = new AtomicBoolean(false);
    private AtomicBoolean ready = new AtomicBoolean(false);

    public TagGraphSerializerImpl(File file) {
        this.file = file;
    }

    @Override
    public AtomicBoolean isReady() {
        return ready;
    }

    /**
     * 从文件中读取模型
     * @return
     */
    public TagGraph generate() {
        //如果正在运行循环等待
        while (running.get()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //如果tagGraph有值则直接返回
        if (tagGraph!=null){
            ready.set(true);
            setChanged();
            notifyObservers();
            return tagGraph;
        }

        //从文件读取模型
        running.set(true);
        if (!file.exists()) {
            try {
                boolean newFile = file.createNewFile();
                if (!newFile){
                    running.set(false);
                    throw new RuntimeException("模型文件无法创建");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (InputStream inputStream = new FileInputStream(file)) {
            if (tagGraph == null) {
                tagGraph = JSON.parseObject(inputStream, TagGraph.class);
                if (tagGraph == null) {
                    tagGraph = new TagGraph();
                    running.set(false);
                    return tagGraph;
                }
                List<Tag> tags = tagGraph.getTags();
                for (Tag tag : tags) {
                    //给 tagEdge 中的 tag 赋值
                    List<TagEdge> edges = tag.getEdges();
                    for (TagEdge edge : edges) {
                        int index = edge.getIndex();
                        if (index>=0){
                            edge.setTag(tagGraph.getTags().get(index));
                        }
                    }

                    //给 mergedTag 中的 List<Tag> 赋值
                    if (tag instanceof MergedTag){
                        MergedTag rootMergedTag = (MergedTag) tag;
                        List<Integer> childrenIndexes = rootMergedTag.getChildrenIndexes();
                        List<Tag> children = new ArrayList<>();
                        for (Integer childrenIndex : childrenIndexes) {
                            children.add(tags.get(childrenIndex));
                        }
                        rootMergedTag.setChildren(children);
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            tagGraph = new TagGraph();
        }
        ready.set(true);
        setChanged();
        notifyObservers();
        running.set(false);
        Log.d("afterGenerate",tagGraph.toString());
        return tagGraph;
    }

    /**
     * 序列化到文件
     */
    public synchronized void serialize() {
        try (OutputStream outputStream = new FileOutputStream(file)) {

            List<Tag> tags = tagGraph.getTags();
            for (Tag tag : tags) {
                //给 index 赋值
                List<TagEdge> edges = tag.getEdges();
                for (TagEdge edge : edges) {
                    int index = tagGraph.getTags().indexOf(edge.getTag());
                    edge.setIndex(index);
                }

                //给 childrenIndexes 赋值
                if (tag instanceof MergedTag){
                    MergedTag rootMergedTag = (MergedTag) tag;
                    List<Tag> children = rootMergedTag.getChildren();
                    List<Integer> childrenIndex = new ArrayList<>();
                    for (Tag child : children) {
                        int index = tagGraph.getTags().indexOf(child);
                        childrenIndex.add(index);
                    }
                    rootMergedTag.setChildrenIndexes(childrenIndex);
                }
            }


            JSON.writeJSONString(outputStream, tagGraph);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
