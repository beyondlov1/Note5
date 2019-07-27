package com.beyond.note5.presenter;

import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import com.beyond.note5.MyApplication;
import com.beyond.note5.model.PredictModel;
import com.beyond.note5.predict.bean.Tag;
import com.beyond.note5.utils.PreferenceUtil;
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

    private Handler handler = new Handler();

    public PredictPresenterImpl(@Nullable PredictView predictView) {
        this.predictView = predictView;
        this.predictModel = MyApplication.getInstance().getPredictModel();
        this.executorService = MyApplication.getInstance().getExecutorService();
    }

    @Override
    public void predict(final String source) {
        if (!PreferenceUtil.getBoolean(MyApplication.TODO_SHOULD_TRAIN,true)){
            return;
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                List<Tag> data = predictModel.predict(source);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (data == null) {
                            onPredictFail();
                        } else {
                            onPredictSuccess(data, source);
                        }
                    }
                });

            }
        });
    }

    @Override
    public void onPredictSuccess(List<Tag> data, String source) {
        if (predictView == null) {
            return;
        }
        predictView.onPredictSuccess(data, source);
    }

    @Override
    public void onPredictFail() {
        if (predictView == null) {
            return;
        }
        predictView.onPredictFail();
    }

    @Override
    public void train(final String source) {
        if (PreferenceUtil.getBoolean(MyApplication.TODO_SHOULD_TRAIN,true)){
            return;
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    predictModel.train(source);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onTrainSuccess();
                        }
                    });
                } catch (Exception e) {
                    Log.e("predictPresenterImpl", "训练失败");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onTrainFail();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onTrainSuccess() {
        if (predictView == null) {
            return;
        }
        predictView.onTrainSuccess();
    }

    @Override
    public void onTrainFail() {
        if (predictView == null) {
            return;
        }
        predictView.onTrainFail();
    }
}
