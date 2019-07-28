package com.beyond.note5.sync.datasource;

import java.io.IOException;

/**
 * @author: beyond
 * @date: 2019/7/28
 */

public interface FileStore {
    void upload(String url, String path) throws IOException;
    void download(String url, String path) throws IOException;
}
