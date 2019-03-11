package com.beyond.note5.predict;

import com.beyond.note5.predict.bean.Callback;
import com.beyond.note5.predict.bean.Tag;
import com.beyond.note5.predict.bean.TagGraph;

import java.io.File;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 主类
 * @author beyondlov1
 * @date 2019/03/11
 */
public class TagPredictor implements Observer {

    private TagGraph tagGraph;
    private TagTrainer tagTrainer ;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public AtomicBoolean isReady = new AtomicBoolean(false);

    public TagPredictor(File file){
        DefaultTagGraphSerializer serializer = new DefaultTagGraphSerializer(file);
        serializer.addObserver(this);
        TagGraphInjector injector = new TagGraphInjectorImpl(serializer);

        this.tagTrainer = new TagTrainer(serializer,injector);
        this.tagGraph = tagTrainer.getTagGraph();
    }

    /**
     * 异步方法
     * @param content
     * @return
     */
    public void predict(final String content, final Callback<List<Tag>> callback) {
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
                    callback.onSuccess(tagGraph.predict(content));
                }else {
                    callback.onFail();
                }
            }
        });
    }

    public TagTrainer getTagTrainer() {
        return tagTrainer;
    }

    @Override
    public void update(Observable o, Object arg) {
        isReady.set(true);
    }
}
