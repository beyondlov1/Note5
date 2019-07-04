package com.beyond.note5.sync.datasource.impl;

import com.beyond.note5.sync.datasource.DavPathStrategy;
import com.beyond.note5.utils.OkWebDavUtil;

import org.apache.commons.lang3.StringUtils;

public class DefaultDavPathStrategy implements DavPathStrategy {

    private String server;

    private String[] paths;

    public DefaultDavPathStrategy(String server,String... paths) {
        this.paths = paths;
        this.server = server;
    }

    @Override
    public String getStoragePath(String id) {
        if (paths.length == 0) {
            throw new RuntimeException("url不能为空");
        }
        if (paths.length == 1) {
            if (paths[0].endsWith("/")) {
                return StringUtils.substringBeforeLast(paths[0], "/");
            }
            return paths[0];
        }

        int index = Math.abs(id.hashCode()) % paths.length;
        if (paths[index].endsWith("/")) {
            return StringUtils.substringBeforeLast(paths[index], "/");
        }
        return paths[index];
    }

    @Override
    public String getStorageUrl(String id) {
        return OkWebDavUtil.concat(server,getStoragePath(id));
    }
}
