package com.beyond.note5.sync;

import com.beyond.note5.sync.datasource.DataSource;

import org.apache.commons.lang3.StringUtils;

/**
 * @author: beyond
 * @date: 2019/7/28
 */

public class SyncContext {
    private DataSource dataSource1;
    private DataSource dataSource2;

    public SyncContext(DataSource dataSource1, DataSource dataSource2) {
        this.dataSource1 = dataSource1;
        this.dataSource2 = dataSource2;
    }

    public String getCorrespondKey(String key) {
        return getCorrespondDataSource(key).getKey();
    }

    public String getCorrespondKey(DataSource dataSource) {
        return getCorrespondDataSource(dataSource).getKey();
    }

    public DataSource getCorrespondDataSource(DataSource dataSource) {
        return getCorrespondDataSource(dataSource.getKey());
    }

    public DataSource getCorrespondDataSource(String key) {
        if (StringUtils.equals(key, dataSource1.getKey())) {
            return dataSource2;
        }
        if (StringUtils.equals(key, dataSource2.getKey())) {
            return dataSource1;
        }
        throw new RuntimeException("can not find target dataSource");
    }
}
