package com.beyond.note5.model;

import com.beyond.note5.predict.TagPredictor;
import com.beyond.note5.predict.bean.Tag;
import com.beyond.note5.predict.bean.TagGraph;
import com.beyond.note5.predict.train.target.TrainTagTarget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author beyondlov1
 * @date 2019/03/30
 */
public class PredictModelImpl implements PredictModel{

    private TagPredictor<String, TagGraph> tagPredictor;

    private static Map<TagPredictor<String, TagGraph> , PredictModel> instanceContainer = new HashMap<>(2);

    public static PredictModel getRelativeSingletonInstance(TagPredictor<String, TagGraph> tagPredictor){
        if (instanceContainer.get(tagPredictor) == null) {
            synchronized (PredictModel.class) {
                PredictModel predictModel = new PredictModelImpl(tagPredictor);
                instanceContainer.put(tagPredictor, predictModel);
                return predictModel;
            }
        }
        return instanceContainer.get(tagPredictor);
    }


    public PredictModelImpl(TagPredictor<String, TagGraph> tagPredictor) {
        this.tagPredictor = tagPredictor;
    }

    @Override
    public List<Tag> predict(String source) {
        return tagPredictor.predictSync(source);
    }

    @Override
    public void train(String source) throws Exception {
        tagPredictor.trainSync(new TrainTagTarget(source));
    }

}
