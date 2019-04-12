package com.beyond.note5.predict.params;

import android.util.Log;

import com.beyond.note5.predict.bean.Tag;
import com.beyond.note5.predict.bean.TagGraph;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author beyondlov1
 * @date 2019/03/13
 */
public abstract class AbstractTagPredictCallback implements TagPredictCallback<String, TagGraph> {
    @Override
    public void onSuccess(String s, TagGraph tagGraph) {
        List<Tag> result = new ArrayList<>();
        if (StringUtils.isBlank(s)){
            List<Tag> tags = tagGraph.getTags();
            result.addAll(tags);
        }else {
            List<Tag> tags = tagGraph.predict(s);
            result.addAll(tags);
        }
        handleResult(result);
    }

    protected abstract void handleResult(List<Tag> tags);

    @Override
    public void onFail() {
        Log.e("AbstractTagPredictCallback","预测失败");
    }
}
