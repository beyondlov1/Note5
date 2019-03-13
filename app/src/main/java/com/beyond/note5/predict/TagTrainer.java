package com.beyond.note5.predict;

import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.beyond.note5.predict.bean.SegResponse;
import com.beyond.note5.predict.bean.Tag;
import com.beyond.note5.predict.bean.TagEdge;
import com.beyond.note5.predict.bean.TagGraph;
import com.beyond.note5.predict.utils.TagUtils;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author beyondlov1
 * @date 2019/03/11
 */
public class TagTrainer {

    private TagGraphSerializer serializer;
    private TagGraphInjector injector;
    private OkHttpClient okHttpClient;

    public TagTrainer(TagGraphSerializer serializer,TagGraphInjector injector) {
        this.serializer = serializer;
        this.injector = injector;
        this.okHttpClient = new OkHttpClient();
    }

    public void train(final String content){
        final TagGraph tagGraph = getTagGraph();

        String url = "http://www.sogou.com/labs/webservice/sogou_word_seg.php?q=" + content;

        RequestBody requestBody = new FormBody.Builder().add("q", content).add("fmt", "js").build();
        final Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            public void onFailure(Call call, IOException e) {
                System.out.println("fail");
            }

            public void onResponse(Call call, Response response) throws IOException {
                List<String> list = getTrainSource(response);
                if (list == null) return;
                processMergedWord(content,list);
                injector.inject(list);
                mergeSingleTags(tagGraph.getTags());
                Log.d("afterTrain",tagGraph.toString());
                serializer.serialize();
            }
        });
    }

    /**
     * 处理合并过的词语， 主要是来加分
     * @param content 传入内容
     * @param list 分词列表
     */
    private void processMergedWord(String content, List<String> list) {

        //思路： 找到分词列表中不存在的合并单词， 给相应的root加分， 找到相应的edge加分， 加分+2
        TagGraph tagGraph = getTagGraph();
        for (Tag tag : tagGraph.getTags()) {
            if (content.contains(tag.getContent())&&!list.contains(tag.getContent())){
                Tag foundTag = getTagGraph().find(tag.getContent());
                if (foundTag!=null){// 能找到
                    System.out.println("foundTagContent "+foundTag.getContent());
                    foundTag.setScore(foundTag.getScore()+2); // 对于这种小几率事件 权重大些
                    Tag prevTag = getPrevTag(content, foundTag, list); //查找前一个tag
                    System.out.println("prevTag "+prevTag);
                    if (prevTag!=null){
                        System.out.println("prevTagContent "+prevTag.getContent());
                        TagEdge foundEdge = prevTag.findEdge(foundTag);
                        System.out.println("foundEdge "+foundEdge);
                        if (foundEdge!=null){
                            System.out.println("foundEdgeContent "+foundEdge.getTag().getContent());
                            foundEdge.setScore(foundEdge.getScore()+1); //给前一个tag的对应edge score+1
                        }
                    }
                }
            }
        }
    }

    private Tag getPrevTag(String content,Tag foundTag,List<String> list){
        int index = content.indexOf(foundTag.getContent());
        int tmp = 0;
        for (String s : list) {
            tmp += s.length();
            if (tmp == index){
                return getTagGraph().find(s);
            }
        }
        return null;
    }

    /**
     * 从接口返回值中提取分词内容
     * @param response
     * @return
     * @throws IOException
     */
    private List<String> getTrainSource(Response response) throws IOException {
        if (response.body() == null) return null;
        List<String> list = new ArrayList<String>();
        SegResponse segResponse = JSON.parseObject(TagUtils.unicodeToString(response.body().string()), SegResponse.class);
        String[][] result = segResponse.getResult();
        for (String[] strings : result) {
            list.add(strings[0]);
        }
        return list;
    }

    /**
     * 合并单个词
     * @param roots graph中的根（也就是所有出现过的词）
     */
    private void mergeSingleTags(List<Tag> roots) {
        List<Tag> singleTags = new ArrayList<Tag>();
        for (Tag next : roots) {
            if (next.getEdges().size() == 1) {
                singleTags.add(next);
            }
        }

        List<Tag> resultTags= new ArrayList<Tag>();
        while (singleTags.size()>0){
            StringBuilder stringBuilder = new StringBuilder();
            List<Tag> walkedTags = new ArrayList<>();
            Tag startTag = singleTags.get(0);
            chain(singleTags,walkedTags,startTag);
            for (Tag walkedTag : walkedTags) {
                stringBuilder.append(walkedTag.getContent());
            }
            Tag foundTag = null;
            for (Tag root : roots) {
                if (StringUtils.equals(root.getContent(),stringBuilder.toString())){
                    foundTag = root;
                    break;
                }
            }
            if (foundTag==null) { // roots中不存在
                Tag tag = new Tag();
                tag.setId(TagUtils.uuid());
                tag.setName(stringBuilder.toString());
                tag.setContent(stringBuilder.toString());
                resultTags.add(tag);

                for (Tag root : roots) {
                    List<TagEdge> edges = root.getEdges();
                    for (TagEdge edge : edges) {
                        if (edge.getTag() == startTag) {
                            TagEdge tagEdge = new TagEdge();
                            tagEdge.setId(TagUtils.uuid());
                            tagEdge.setTag(tag);
                            root.getEdges().add(tagEdge);
                            break;
                        }
                    }
                }
            }
//            else{// roots中存在
//                foundTag.setScore(foundTag.getScore()+1);
//
//                for (Tag root : roots) {
//                    List<TagEdge> edges = root.getEdges();
//                    for (TagEdge edge : edges) {
//                        if (edge.getTag() == foundTag) {
//                            edge.setScore(edge.getScore()+1);
//                            break;
//                        }
//                    }
//                }
//            }

        }
        roots.addAll(resultTags);
    }

    /**
     * 串联单个的词， 并从原列表中删除
     * @param singleTags 原来单个词的列表
     * @param walkedTags 遍历过的tag，防止死循环： 1-2-3-1
     * @param tag 下一个单个词
     */
    private void chain(List<Tag> singleTags,List<Tag> walkedTags, Tag tag) {
        if (walkedTags.contains(tag)){
            return;
        }
        walkedTags.add(tag);
        singleTags.remove(tag);
        if (tag.getEdges().size() == 1) {
            chain(singleTags,walkedTags, tag.getEdges().get(0).getTag());
        }
    }

    public TagGraphSerializer getSerializer() {
        return serializer;
    }

    public TagGraph getTagGraph() {
        return serializer.generate();
    }
}
