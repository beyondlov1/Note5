package com.beyond.note5.predict.serializer;

import com.beyond.note5.predict.bean.TagGraph;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author beyondlov1
 * @date 2019/03/10
 */
public interface TagGraphSerializer {
    AtomicBoolean isReady();
    TagGraph generate();
    void serialize();
}
