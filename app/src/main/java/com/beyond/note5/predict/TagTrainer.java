package com.beyond.note5.predict;

import com.alibaba.fastjson.JSON;
import com.beyond.note5.predict.bean.SegResponse;
import com.beyond.note5.predict.bean.Tag;
import com.beyond.note5.predict.bean.TagGraph;
import com.beyond.note5.predict.utils.TagUtils;
import okhttp3.*;

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

    public void train(String content){
        final TagGraph tagGraph = serializer.generate();

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
                injector.inject(list);
                mergeSingleTags(tagGraph.getTags());
                serializer.serialize();
            }
        });
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
            chain(singleTags,singleTags.get(0),stringBuilder);
            Tag tag = new Tag();
            tag.setId(TagUtils.uuid());
            tag.setName(stringBuilder.toString());
            tag.setContent(stringBuilder.toString());
            resultTags.add(tag);
        }
        roots.addAll(resultTags);
    }

    /**
     * 串联单个的词， 并从原列表中删除
     * @param tags 原来单个词的列表
     * @param tag 下一个单个词
     * @param stringBuilder 串联的结果
     */
    private void chain(List<Tag> tags, Tag tag, StringBuilder stringBuilder) {
        stringBuilder.append(tag.getContent());
        tags.remove(tag);
        if (tag.getEdges().size() == 1) {
            chain(tags, tag.getEdges().get(0).getTag(), stringBuilder);
        }
    }

    public TagGraphSerializer getSerializer() {
        return serializer;
    }

    public TagGraph getTagGraph() {
        return serializer.generate();
    }
}
