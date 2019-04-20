package com.beyond.note5.presenter;

import android.util.Log;

import com.beyond.note5.MyApplication;
import com.beyond.note5.model.PredictModel;
import com.beyond.note5.predict.bean.Tag;
import com.beyond.note5.view.PredictView;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author beyondlov1
 * @date 2019/03/30
 */
public class PredictPresenterImpl implements PredictPresenter {

    private PredictModel predictModel;
    private PredictView predictView;

    private ExecutorService executorService;

    public PredictPresenterImpl(PredictView predictView) {
        this.predictView = predictView;
        this.predictModel = MyApplication.getInstance().getPredictModel();
        this.executorService = MyApplication.getInstance().getExecutorService();
    }

    @Override
    public void predict(final String source) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                List<Tag> data = predictModel.predict(source);
                if (data == null){
                    onPredictFail();
                }else {
                    onPredictSuccess(data,source);
                }
            }
        });
    }

    @Override
    public void onPredictSuccess(List<Tag> data,String source) {
        predictView.onPredictSuccess(data,source);
    }

    @Override
    public void onPredictFail() {
        predictView.onPredictFail();
    }

    @Override
    public void train(final String source) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    predictModel.train(source);
                    onTrainSuccess();
                }catch (Exception e){
                    Log.e("predictPresenterImpl","训练失败");
                    onTrainFail();
                }
            }
        });
    }

    @Override
    public void onTrainSuccess() {
        predictView.onTrainSuccess();
    }

    @Override
    public void onTrainFail() {
        predictView.onTrainFail();
    }
}
