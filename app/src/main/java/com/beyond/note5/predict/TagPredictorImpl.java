package com.beyond.note5.predict;

import com.beyond.note5.predict.bean.TagGraph;

import java.io.File;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 主类
 * @author beyondlov1
 * @date 2019/03/11
 */
public class TagPredictorImpl implements TagPredictor<String,TagGraph>,Observer {

    private TagGraph tagGraph;
    private TagTrainer tagTrainer ;
    private ExecutorService executorService;

    public AtomicBoolean isReady = new AtomicBoolean(false);

    public TagPredictorImpl(File file){
        TagGraphSerializerImpl serializer = new TagGraphSerializerImpl(file);
        serializer.addObserver(this);

        this.tagTrainer = TagTrainer.create(serializer);
        this.tagGraph = serializer.generate();
    }

    /**
     * 异步方法
     * @param content 预测内容
     */
    public void predict(final String content, final Callback<String,TagGraph> callback) {
        if (isReady.get()&&tagGraph == null){ // 试图解决缓存失效的问题， 不知道管不管用
            this.tagGraph = getTagTrainer().getTagGraph();
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                while (!isReady.get()&&count<80){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    count++;
                }
                if (count<80){
                    callback.onSuccess(content,tagGraph);
                }else {
                    callback.onFail();
                }
            }
        });
    }

    public TagTrainer getTagTrainer() {
        return tagTrainer;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void update(Observable o, Object arg) {
        isReady.set(true);
    }
}
