package com.beyond.note5.sync.webdav.client;

import com.beyond.note5.utils.OkWebDavUtil;
import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.SardineException;
import com.thegrizzlylabs.sardineandroid.impl.handler.OkHttpSardine2;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Response;

import static com.beyond.note5.utils.OkWebDavUtil.getRootUrl;

public class SardineDavClient implements DavClient {

    private final static Map<String, Boolean> IS_DIR_EXIST = new ConcurrentHashMap<>();

    private Sardine sardine;

    private AtomicInteger failCount = new AtomicInteger(0);

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
        try {
            sardine.put(url, content.getBytes());
            failCount.set(0);
        } catch (SardineException e) {
            if (e.getStatusCode() == 409) {
                if (failCount.get() > 2) {
                    throw e;
                }
                IS_DIR_EXIST.remove(dirUrl);
                closeResponse();
                failCount.getAndAdd(1);
                put(url, content);
            } else {
                throw e;
            }
        } finally {
            closeResponse();
        }
    }

    @Override
    public void upload(String path, String url) throws IOException {
        upload(new File(path), url);
    }

    @Override
    public void upload(File file, String url) throws IOException {
        upload(file, url, null);
    }

    @Override
    public void upload(File file, String url, String contentType) throws IOException {
        String dirUrl = StringUtils.substringBeforeLast(url, "/");
        mkDir(dirUrl);
        try {
            sardine.put(url, file, contentType);
            failCount.set(0);
        } catch (SardineException e) {
            if (e.getStatusCode() == 409) {
                if (failCount.get() > 2) {
                    throw e;
                }
                IS_DIR_EXIST.remove(dirUrl);
                closeResponse();
                failCount.getAndAdd(1);
                sardine.put(url, file, contentType);
            } else {
                throw e;
            }
        } finally {
            closeResponse();
        }
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
    public void download(String url, String path) throws IOException {
        download(url, new File(path));
    }

    @Override
    public void download(String url, File file) throws IOException {
        try (InputStream inputStream = sardine.get(url);
             FileOutputStream output = new FileOutputStream(file)) {
            IOUtils.copy(inputStream, output);
            output.flush();
        } finally {
            closeResponse();
        }
    }

    /**
     * 这里webdav访问如果同一时间访问多次， 会产生503的错误， 所以不得已改成同步的
     *
     * @param dirUrl
     * @return
     * @throws IOException
     */
    @Override
    public List<String> listAllFileName(String dirUrl) throws IOException {
        return listAllFileName(dirUrl, null);
    }

    @Override
    public synchronized List<String> listAllFilePath(String dirUrl) throws IOException {
        return listAllFilePath(dirUrl, null);
    }

    @Override
    public List<String> listAllFileUrl(String dirUrl) throws IOException {
        return listAllFileUrl(dirUrl, null);
    }

    @Override
    public synchronized List<String> listAllFileName(String dirUrl, DavFilter filter) throws IOException {
        List<DavResource> davResources = listAllFileResource(dirUrl);
        Iterator<DavResource> iterator = davResources.iterator();
        while (iterator.hasNext()) {
            DavResource davResource = iterator.next();
            if (filter != null && filter.filter(davResource)) {
                iterator.remove();
            }
        }
        List<String> result = new ArrayList<>();
        for (DavResource davResource : davResources) {
            result.add(davResource.getName());
        }
        return result;
    }

    @Override
    public synchronized List<String> listAllFilePath(String dirUrl, DavFilter filter) throws IOException {
        List<DavResource> davResources = listAllFileResource(dirUrl);
        Iterator<DavResource> iterator = davResources.iterator();
        while (iterator.hasNext()) {
            DavResource davResource = iterator.next();
            if (filter != null && filter.filter(davResource)) {
                iterator.remove();
            }
        }
        List<String> result = new ArrayList<>();
        for (DavResource davResource : davResources) {
            result.add(davResource.getPath());
        }
        return result;
    }

    @Override
    public List<String> listAllFileUrl(String dirUrl, DavFilter filter) throws IOException {
        List<String> paths = listAllFilePath(dirUrl, filter);
        String prefix = getRootUrl(dirUrl);
        List<String> result = new ArrayList<>(paths.size());
        for (String path : paths) {
            result.add(prefix + path);
        }
        return result;
    }

    public synchronized List<DavResource> listAllFileResource(String dirUrl) throws IOException {

        List<DavResource> result = new ArrayList<>();
        mkDir(dirUrl);
        List<DavResource> list = sardine.list(dirUrl);
        for (DavResource davResource : list) {
            if (OkWebDavUtil.urlEquals(
                    getRootUrl(dirUrl) + davResource.getPath(),
                    dirUrl)) {
                continue;
            }
            if (davResource.isDirectory()) {
                List<DavResource> subResource = listAllFileResource(
                        getRootUrl(dirUrl) + davResource.getPath());
                result.addAll(subResource);
                continue;
            }
            result.add(davResource);
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
        if (IS_DIR_EXIST.get(dirUrl) != null && IS_DIR_EXIST.get(dirUrl)) {
            return true;
        }
        //获取文件夹路径
        String parentUrl = StringUtils.substringBeforeLast(dirUrl, "/");
        String root = OkWebDavUtil.getRootUrl(dirUrl);
        if (!OkWebDavUtil.urlEquals(parentUrl, root)) {
            mkDir(parentUrl);
        }
        try {
            sardine.createDirectory(dirUrl);
            IS_DIR_EXIST.put(dirUrl, true);
            return true;
        } catch (IOException e) {
            //ignore
            return false;
        } finally {
            if (sardine instanceof OkHttpSardine2) {
                ((OkHttpSardine2) sardine).closeCurrentResponse();
            }
        }
    }

    private void closeResponse() {
        if (sardine instanceof OkHttpSardine2) {
            ((OkHttpSardine2) sardine).closeCurrentResponse();
        }
    }

    private Response getResponse() {
        if (sardine instanceof OkHttpSardine2) {
            ((OkHttpSardine2) sardine).getResponse();
        }
        throw new RuntimeException("not supported");
    }

}
