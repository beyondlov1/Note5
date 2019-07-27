package com.beyond.note5.model;

import com.beyond.note5.predict.Predictor;
import com.beyond.note5.predict.bean.Tag;
import com.beyond.note5.predict.bean.TagGraph;
import com.beyond.note5.predict.train.TrainSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author beyondlov1
 * @date 2019/03/30
 */
public class PredictModelImpl implements PredictModel{

    private Predictor<String, TagGraph> predictor;

    private static Map<Predictor<String, TagGraph>, PredictModel> instanceContainer = new HashMap<>(2);

    public static PredictModel getRelativeSingletonInstance(Predictor<String, TagGraph> predictor){
        if (instanceContainer.get(predictor) == null) {
            synchronized (PredictModel.class) {
                PredictModel predictModel = new PredictModelImpl(predictor);
                instanceContainer.put(predictor, predictModel);
                return predictModel;
            }
        }
        return instanceContainer.get(predictor);
    }


    public PredictModelImpl(Predictor<String, TagGraph> predictor) {
        this.predictor = predictor;
    }

    @Override
    public List<Tag> predict(String source) {
        return predictor.predictSync(source);
    }

    @Override
    public void train(String source) throws Exception {
        predictor.trainSync(new TrainSource(source));
    }

}
