package com.beyond.note5.predict;

import com.alibaba.fastjson.JSON;
import com.beyond.note5.predict.bean.Tag;
import com.beyond.note5.predict.bean.TagEdge;
import com.beyond.note5.predict.bean.TagGraph;

import java.io.*;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author beyondlov1
 * @date 2019/03/10
 */
public class DefaultTagGraphSerializer extends Observable implements TagGraphSerializer {

    private TagGraph tagGraph;
    private File file;
    private AtomicBoolean ready = new AtomicBoolean(false);

    public DefaultTagGraphSerializer(File file) {
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
        if (tagGraph!=null){
            return tagGraph;
        }
        if (!file.exists()) {
            try {
                boolean newFile = file.createNewFile();
                if (!newFile){
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
                    return tagGraph;
                }
                List<Tag> tags = tagGraph.getTags();
                for (Tag tag : tags) {
                    List<TagEdge> edges = tag.getEdges();
                    for (TagEdge edge : edges) {
                        int index = edge.getIndex();
                        if (index>=0){
                            edge.setTag(tagGraph.getTags().get(index));
                        }
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
        return tagGraph;
    }

    /**
     * 序列化到文件
     */
    public void serialize() {
        try (OutputStream outputStream = new FileOutputStream(file)) {
            List<Tag> tags = tagGraph.getTags();
            for (Tag tag : tags) {
                List<TagEdge> edges = tag.getEdges();
                for (TagEdge edge : edges) {
                    int index = tagGraph.getTags().indexOf(edge.getTag());
                    edge.setIndex(index);
                }
            }
            JSON.writeJSONString(outputStream, tagGraph);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
