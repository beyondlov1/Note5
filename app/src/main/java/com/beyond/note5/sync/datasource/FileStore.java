package com.beyond.note5.sync.datasource;

import java.io.IOException;

/**
 * @author: beyond
 * @date: 2019/7/28
 */

public interface FileStore {
    void upload(String id, String localPath) throws IOException;
    void download(String id, String localPath) throws IOException;
}
