package com.beyond.note5.predict.train.injector;

import com.beyond.note5.predict.serializer.TagGraphSerializer;
import com.beyond.note5.predict.bean.Tag;
import com.beyond.note5.predict.bean.TagEdge;
import com.beyond.note5.predict.bean.TagGraph;
import com.beyond.note5.predict.utils.TagUtils;

import java.util.List;

/**
 * @author beyondlov1
 * @date 2019/03/10
 */
public class TagGraphInjectorImpl implements TagGraphInjector {

    private TagGraph graph;
    private TagGraphSerializer serializer;

    public TagGraphInjectorImpl(TagGraph graph) {
        this.graph = graph;
    }
    public TagGraphInjectorImpl(TagGraphSerializer serializer) {
        this.serializer = serializer;
        init();
    }

    private void init(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                graph = serializer.generate();
            }
        });
        thread.start();
    }


    public void inject(List<String> values) {
        while (!serializer.isReady().get()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String lastValue = null;
        int i = 0;
        for (String value : values) {
            Tag prevTag = graph.find(lastValue);
            Tag currTag = graph.find(value);

            if (prevTag == null) { //第一个
                if (currTag == null) { //新词
                    currTag = TagUtils.createTag(value);
                    graph.getTags().add(currTag);
                } else { // 旧词
                    currTag.setScore(currTag.getScore() + 1);
                }
            } else { // 不是第一个
                if (currTag == null) { // 新加入一个词
                    currTag = TagUtils.createTag(value);
                    graph.getTags().add(currTag);

                    TagEdge tagEdge = TagUtils.createTagEdge(currTag, graph.getTags());
                    prevTag.getEdges().add(tagEdge);
                } else { // 旧词
                    TagEdge foundEdge = prevTag.findEdge(currTag);
                    if (foundEdge==null){
                        TagEdge tagEdge = TagUtils.createTagEdge(currTag, graph.getTags());
                        prevTag.getEdges().add(tagEdge);
                    }else{
                        foundEdge.setScore(foundEdge.getScore()+1);
                    }
                    currTag.setScore(currTag.getScore()+1);
                }
            }

            if (i != 0&&currTag.isFirst()){
                currTag.setFirst(false);
            }else if (i==0&&!currTag.isFirst()&&currTag.getScore()>2){
                currTag.setFirst(true);
            }

            i++;
            lastValue = value;
        }
    }

}
