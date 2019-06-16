package com.beyond.note5.sync.webdav.client;

import com.beyond.note5.utils.OkWebDavUtil;
import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.handler.OkHttpSardine2;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class SardineDavClient implements DavClient {

    private Sardine sardine;

    public SardineDavClient(Sardine sardine) {
        this.sardine = sardine;
    }

    public SardineDavClient(String username, String password) {
        this.sardine = new OkHttpSardine2();
        sardine.setCredentials(username, password);
    }

    @Override
    public void put(String url, String content) throws IOException {
        String dirUrl = StringUtils.substringBeforeLast(url, "/");
        OkWebDavUtil.mkRemoteDir(sardine, dirUrl);
        sardine.put(url, content.getBytes());
        closeResponse();
    }

    @Override
    public String get(String url) throws IOException {
        try (InputStream inputStream = sardine.get(url)) {
            return IOUtils.toString(inputStream, Charset.defaultCharset());
        } finally {
            closeResponse();
        }
    }

    @Override
    public List<String> listAllFileName(String dirUrl) throws IOException {
        List<String> result = new ArrayList<>();
        OkWebDavUtil.mkRemoteDir(sardine, dirUrl);
        List<DavResource> list = sardine.list(dirUrl, 1);
        for (DavResource davResource : list) {
            if (StringUtils.equals(
                    ("https://" + URI.create(dirUrl).getHost() + davResource.getPath()).replace("/", ""),
                    dirUrl.replace("/", ""))) {
                continue;
            }
            if (davResource.isDirectory()) {
                List<String> subNames = listAllFileName(
                        "https://" + URI.create(dirUrl).getHost() + davResource.getPath());
                result.addAll(subNames);
                continue;
            }
            result.add(davResource.getName());
        }
        closeResponse();
        return result;
    }

    @Override
    public List<String> listAllFilePath(String dirUrl) throws IOException {
        List<String> result = new ArrayList<>();
        List<DavResource> list = sardine.list(dirUrl, 1);
        for (DavResource davResource : list) {
            if (StringUtils.equals("https://" + OkWebDavUtil.concat(URI.create(dirUrl).getHost() + davResource.getPath(), "/"), dirUrl)) {
                continue;
            }
            if (davResource.isDirectory()) {
                List<String> subNames = listAllFilePath("https://" + OkWebDavUtil.concat(URI.create(dirUrl).getHost() + davResource.getPath(), "/"));
                result.addAll(subNames);
                continue;
            }
            result.add(davResource.getPath());
        }
        closeResponse();
        return result;
    }

    @Override
    public void delete(String url) throws IOException {
        sardine.delete(url);
        closeResponse();
    }

    private void closeResponse() {
        if (sardine instanceof OkHttpSardine2) {
            ((OkHttpSardine2) sardine).closeCurrentResponse();
        }
    }

}
