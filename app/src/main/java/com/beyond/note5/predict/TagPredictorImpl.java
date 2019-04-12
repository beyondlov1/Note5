package com.beyond.note5.predict;

import android.util.Log;

import com.beyond.note5.predict.bean.Tag;
import com.beyond.note5.predict.bean.TagGraph;
import com.beyond.note5.predict.params.TagPredictCallback;
import com.beyond.note5.predict.serializer.TagGraphSerializerImpl;
import com.beyond.note5.predict.train.FilterableTagTrainer;
import com.beyond.note5.predict.train.TagTrainer;
import com.beyond.note5.predict.train.TagTrainerImpl;
import com.beyond.note5.predict.train.filter.AbstractTrainTagFilter;
import com.beyond.note5.predict.train.target.TrainTagTarget;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 主类
 *
 * @author beyondlov1
 * @date 2019/03/11
 */
@SuppressWarnings("WeakerAccess")
public class TagPredictorImpl implements TagPredictor<String, TagGraph>, Observer {

    private TagGraph tagGraph;
    private TagTrainer tagTrainer;
    private ExecutorService executorService;

    private AtomicBoolean isReady = new AtomicBoolean(false);

    public TagPredictorImpl(File file) {
        TagGraphSerializerImpl serializer = new TagGraphSerializerImpl(file);
        serializer.addObserver(this);

        this.tagTrainer = TagTrainerImpl.create(serializer);
        this.tagGraph = serializer.generate();
    }

    public TagPredictorImpl(File file,boolean isFilterable) {
        TagGraphSerializerImpl serializer = new TagGraphSerializerImpl(file);
        serializer.addObserver(this);

        TagTrainer tagTrainer = TagTrainerImpl.create(serializer);
        if (isFilterable){
            this.tagTrainer = new FilterableTagTrainer(tagTrainer);
        }else {
            this.tagTrainer = tagTrainer;
        }
        this.tagGraph = serializer.generate();
    }

    /**
     * 异步方法
     *
     * @param content 预测内容
     */
    public void predictAsync(final String content, final TagPredictCallback<String, TagGraph> tagPredictCallback) {
        if (isReady.get() && tagGraph == null) { // 试图解决缓存失效的问题， 不知道管不管用
            this.tagGraph = tagTrainer.getTagGraph();
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                while (!isReady.get() && count < 80) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    count++;
                }
                if (count < 80) {
                    tagPredictCallback.onSuccess(content, tagGraph);
                } else {
                    tagPredictCallback.onFail();
                }
            }
        });
    }

    @Override
    public List<Tag> predictSync(String source) {
        int count = 0;
        while (!isReady.get() && count < 80) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }

        if (count < 80) {
            List<Tag> result = new ArrayList<>();
            if (StringUtils.isBlank(source)) {
                List<Tag> tags = tagGraph.getTags();
                result.addAll(tags);
            } else {
                List<Tag> tags = tagGraph.predict(source);
                result.addAll(tags);
            }
            return result;
        } else {
            return null;
        }
    }

    @Override
    public void addTrainFilter(AbstractTrainTagFilter filter) {
        if (tagTrainer instanceof FilterableTagTrainer){
            ((FilterableTagTrainer) tagTrainer).addFilter(filter);
        }else {
            Log.w("TagPredictImpl","not filterableTagTrainer, can not add filter");
        }
    }

    @Override
    public void trainSync(TrainTagTarget trainTagTarget) throws Exception {
        tagTrainer.trainSync(trainTagTarget);
    }

    @Override
    public void trainAsync(TrainTagTarget trainTagTarget) throws Exception {
        tagTrainer.trainAsync(trainTagTarget);
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void update(Observable o, Object arg) {
        isReady.set(true);
    }
}
