package com.beyond.note5.sync.datasource;

import com.beyond.note5.sync.datasource.entity.SyncStamp;

import java.io.IOException;

/**
 * @author: beyond
 * @date: 2019/8/3
 */

public interface SyncStampModel {
    void update(SyncStamp syncStamp) throws IOException;
    SyncStamp retrieve() throws IOException;
}
