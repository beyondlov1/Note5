package com.beyond.note5.sync.datasource;

public interface DavPathStrategy {
    String getStoragePath(String id);
    String getStorageUrl(String id);
}
