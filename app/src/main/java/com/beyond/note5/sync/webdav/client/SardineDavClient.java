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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Response;

public class SardineDavClient implements DavClient {

    private final static Map<String,Boolean> IS_DIR_EXIST = new ConcurrentHashMap<>();

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
        mkDir(dirUrl);
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

    /**
     * 这里webdav访问如果同一时间访问多次， 会产生503的错误， 所以不得已改成同步的
     * @param dirUrl
     * @return
     * @throws IOException
     */
    @Override
    public List<String> listAllFileName(String dirUrl) throws IOException {
        List<String> result = new ArrayList<>();
        mkDir(dirUrl);
        List<DavResource> list = sardine.list(dirUrl);
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
    public synchronized List<String> listAllFilePath(String dirUrl) throws IOException {
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

    @Override
    public boolean exists(String url) throws IOException {
        boolean exists = sardine.exists(url);
        closeResponse();
        return exists;
    }

    @Override
    public boolean mkDir(String dirUrl) {
        if (IS_DIR_EXIST.get(dirUrl)!=null&& IS_DIR_EXIST.get(dirUrl)){
            return true;
        }
        //获取文件夹路径
        String parentUrl = StringUtils.substringBeforeLast(dirUrl, "/");
        String root = "https://" + URI.create(dirUrl).getHost();
        if (!OkWebDavUtil.urlEquals(parentUrl,root)) {
            mkDir(parentUrl);
        }
        try {
            sardine.createDirectory(dirUrl);
            IS_DIR_EXIST.put(dirUrl,true);
            return true;
        }catch (IOException e){
            //ignore
            return false;
        }finally {
            if (sardine instanceof OkHttpSardine2){
                ((OkHttpSardine2) sardine).closeCurrentResponse();
            }
        }
    }

    private void closeResponse() {
        if (sardine instanceof OkHttpSardine2) {
            ((OkHttpSardine2) sardine).closeCurrentResponse();
        }
    }

    private Response getResponse(){
        if (sardine instanceof OkHttpSardine2) {
            ((OkHttpSardine2) sardine).getResponse();
        }
        throw new RuntimeException("not supported");
    }

}
