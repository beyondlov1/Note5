package com.beyond.note5.predict;

import android.util.Log;

import com.beyond.note5.predict.bean.Tag;
import com.beyond.note5.predict.bean.TagGraph;
import com.beyond.note5.predict.params.PredictCallback;
import com.beyond.note5.predict.serializer.TagGraphSerializerImpl;
import com.beyond.note5.predict.train.FilterableTrainer;
import com.beyond.note5.predict.train.Trainer;
import com.beyond.note5.predict.train.TrainerImpl;
import com.beyond.note5.predict.filter.train.TrainFilter;
import com.beyond.note5.predict.train.TrainSource;

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
public class PredictorImpl implements Predictor<String, TagGraph>, Observer {

    private TagGraph tagGraph;
    private Trainer trainer;
    private ExecutorService executorService;

    private AtomicBoolean isReady = new AtomicBoolean(false);

    public PredictorImpl(File file) {
        TagGraphSerializerImpl serializer = new TagGraphSerializerImpl(file);
        serializer.addObserver(this);

        this.trainer = TrainerImpl.create(serializer);
        this.tagGraph = serializer.generate();
    }

    public PredictorImpl(File file, boolean isFilterable) {
        TagGraphSerializerImpl serializer = new TagGraphSerializerImpl(file);
        serializer.addObserver(this);

        Trainer trainer = TrainerImpl.create(serializer);
        if (isFilterable){
            this.trainer = new FilterableTrainer(trainer);
        }else {
            this.trainer = trainer;
        }
        this.tagGraph = serializer.generate();
    }

    /**
     * 异步方法
     *
     * @param content 预测内容
     */
    public void predictAsync(final String content, final PredictCallback<String, TagGraph> predictCallback) {
        if (isReady.get() && tagGraph == null) { // 试图解决缓存失效的问题， 不知道管不管用
            this.tagGraph = trainer.getTagGraph();
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
                    predictCallback.onSuccess(content, tagGraph);
                } else {
                    predictCallback.onFail();
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
    public void addTrainFilter(TrainFilter filter) {
        if (trainer instanceof FilterableTrainer){
            ((FilterableTrainer) trainer).addFilter(filter);
        }else {
            Log.w("TagPredictImpl","not filterableTagTrainer, can not add filter");
        }
    }

    @Override
    public void trainSync(TrainSource trainTagTarget) throws Exception {
        trainer.trainSync(trainTagTarget);
    }

    @Override
    public void trainAsync(TrainSource trainTagTarget) throws Exception {
        trainer.trainAsync(trainTagTarget);
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void update(Observable o, Object arg) {
        isReady.set(true);
    }
}
