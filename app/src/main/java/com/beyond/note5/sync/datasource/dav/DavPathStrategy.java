package com.beyond.note5.sync.datasource.dav;

public interface DavPathStrategy {
    String getStorageDir(String id);
    String getStorageDirUrl(String id);
    String[] getAllStorageDirs(String type);
}
