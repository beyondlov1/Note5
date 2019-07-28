package com.beyond.note5.sync.datasource;

import com.beyond.note5.sync.SyncContextAware;

public interface SqlDataSource<T> extends DataSource<T>,SyncContextAware {
}
