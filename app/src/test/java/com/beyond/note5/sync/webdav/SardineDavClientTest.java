package com.beyond.note5.sync.webdav;

import com.beyond.note5.sync.webdav.client.DavClient;
import com.beyond.note5.sync.webdav.client.SardineDavClient;
import com.beyond.note5.utils.OkWebDavUtil;
import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static com.beyond.note5.sync.webdav.CommonTest.getExecutorService;
import static com.beyond.note5.utils.AsyncUtil.computeAllAsyn;

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
    public void listAllFileName3() throws IOException, ExecutionException, InterruptedException {
        DavClient client = new SardineDavClient(CommonTest.getUsername(),CommonTest.getPassword());

        List<Callable<List<String>>> callables = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            int finalI = i;
            callables.add(new Callable<List<String>>() {
                @Override
                public List<String> call() throws Exception {
                    if (finalI %2 == 0){
                        return client.listAllFileName(OkWebDavUtil.concat(CommonTest.getRootUrl(), "/test/t1/baidu"));
                    }else {
                        return client.listAllFileName(OkWebDavUtil.concat(CommonTest.getRootUrl(), "/test/t1/google"));
                    }
                }
            });
        }
        List<String> list = computeAllAsyn(getExecutorService(), callables);
        System.out.println(list);
    }

    @Test
    public void listAllFilePath() throws IOException {
        DavClient client = new SardineDavClient(CommonTest.getUsername(),CommonTest.getPassword());
        List<String> paths = client.listAllFilePath(OkWebDavUtil.concat(CommonTest.getRootUrl(), "/nut3/todo/splice2/"));
        for (String path : paths) {
            System.out.println(path);
        }
    }

    @Test
    public void listAllFileUrl() throws IOException {
        DavClient client = new SardineDavClient(CommonTest.getUsername(),CommonTest.getPassword());
        List<String> urls = client.listAllFileUrl(OkWebDavUtil.concat(CommonTest.getRootUrl(), "/nut3/todo/splice2/"));
        for (String path : urls) {
            System.out.println(path);
        }
    }

    @Test
    public void listAllFileResource() throws IOException {
        SardineDavClient client = new SardineDavClient(CommonTest.getUsername(),CommonTest.getPassword());
        List<DavResource> resources = client.listAllFileResource(OkWebDavUtil.concat(CommonTest.getRootUrl(), "/nut3/note/"));
        for (DavResource resource : resources) {
            System.out.println(resource.getModified());
        }
    }

    @Test
    public void exists() throws IOException {
        DavClient client = new SardineDavClient(CommonTest.getUsername(),CommonTest.getPassword());
        boolean exists = client.exists(OkWebDavUtil.concat(CommonTest.getRootUrl(), "/test/lok/676"));
        assert !exists;
    }

    @Test
    public void mkDir(){
        DavClient client = new SardineDavClient(CommonTest.getUsername(),CommonTest.getPassword());
        boolean success = client.mkDir(OkWebDavUtil.concat(CommonTest.getRootUrl(), "/NoteCloud2/43"));
        assert success;
    }
}