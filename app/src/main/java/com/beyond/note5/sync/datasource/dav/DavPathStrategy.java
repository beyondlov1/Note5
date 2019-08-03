package com.beyond.note5.sync.datasource.dav;

public interface DavPathStrategy {
    String getStoragePath(String id);
    String getStorageUrl(String id);
    String[] getAllStoragePaths(String type);
}
