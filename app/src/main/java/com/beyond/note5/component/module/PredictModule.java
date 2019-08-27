package com.beyond.note5.component.module;

import com.beyond.note5.presenter.PredictPresenter;
import com.beyond.note5.presenter.PredictPresenterImpl;
import com.beyond.note5.view.PredictView;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author: beyond
 * @date: 2019/8/27
 */

@Module
public class PredictModule {

    private PredictView predictView;

    public PredictModule(PredictView predictView) {
        this.predictView = predictView;
    }

    @Singleton
    @Provides
    PredictPresenter providePredictPresenter(){
        return new PredictPresenterImpl(predictView);
    }
}
