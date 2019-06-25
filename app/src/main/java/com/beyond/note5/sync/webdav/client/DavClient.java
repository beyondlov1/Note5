package com.beyond.note5.sync.webdav.client;

import java.io.IOException;
import java.util.List;

public interface DavClient {
    void put(String url,String content) throws IOException;
    String get(String url) throws IOException;
    List<String> listAllFileName(String dirUrl) throws IOException;
    List<String> listAllFilePath(String dirUrl) throws IOException;
    List<String> listAllFileName(String dirUrl, DavFilter filter) throws IOException;
    List<String> listAllFilePath(String dirUrl, DavFilter filter) throws IOException;
    void delete(String url) throws IOException;
    boolean exists(String url) throws IOException;
    boolean mkDir(String dirUrl);
}
