package com.beyond.note5.sync.datasource;


import com.beyond.note5.sync.datasource.entity.SyncStamp;

import java.io.IOException;
import java.util.Map;

public interface MultiDataSource<T>  extends DataSource<T>{
    void setChosenKey(String key);
    String getChosenKey();
    void initLastSyncStamps() throws IOException;
    Map<String,SyncStamp> getSyncStampsCache();
    void setSyncStampsCache(Map<String, SyncStamp> syncStamps);
}
