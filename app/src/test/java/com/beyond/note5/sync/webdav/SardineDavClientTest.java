package com.beyond.note5.sync.webdav;

import com.beyond.note5.sync.webdav.client.DavClient;
import com.beyond.note5.sync.webdav.client.SardineDavClient;
import com.beyond.note5.utils.OkWebDavUtil;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class SardineDavClientTest {

    @Test
    public void put() throws IOException {
        DavClient client = new SardineDavClient(CommonTest.getUsername(),CommonTest.getPassword());
        String url = OkWebDavUtil.concat(CommonTest.getRootUrl(), "/test/mmcc/test.txt");
        client.put(url,"test");
    }

    @Test
    public void get() throws IOException {
        DavClient client = new SardineDavClient(CommonTest.getUsername(),CommonTest.getPassword());
        String content = client.get(OkWebDavUtil.concat(CommonTest.getRootUrl(), "/test/test.txt"));
        System.out.println(content);
    }

    @Test
    public void listAllFileName() throws IOException {
        DavClient client = new SardineDavClient(CommonTest.getUsername(),CommonTest.getPassword());
        List<String> names = client.listAllFileName(OkWebDavUtil.concat(CommonTest.getRootUrl(), "/test/ddd/iii"));
        for (String name : names) {
            System.out.println(name);
        }
    }

    @Test
    public void listAllFileName2() throws IOException {
        Sardine sardine = new OkHttpSardine();
        sardine.setCredentials(CommonTest.getUsername(), CommonTest.getPassword());
        OkWebDavUtil.mkRemoteDir(sardine, OkWebDavUtil.concat(CommonTest.getRootUrl(), "/test/d454fd/67467/6ty546"));
    }

    @Test
    public void listAllFilePath() throws IOException {
        DavClient client = new SardineDavClient(CommonTest.getUsername(),CommonTest.getPassword());
        List<String> paths = client.listAllFilePath(OkWebDavUtil.concat(CommonTest.getRootUrl(), "/NoteCloud2/"));
        for (String path : paths) {
            System.out.println(path);
        }
    }
}