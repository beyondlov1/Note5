package com.beyond.note5.sync.datasource.dav;

import com.beyond.note5.utils.OkWebDavUtil;

import org.apache.commons.lang3.StringUtils;

import static com.beyond.note5.MyApplication.DAV_DATA_DIR;

public class DefaultDavPathStrategy implements DavPathStrategy {

    private String server;

    private String[] paths;

    private Class clazz;

    public DefaultDavPathStrategy(String server,Class clazz, String... paths) {
        this.server = server;
        this.clazz = clazz;
        this.paths = new String[]{
                OkWebDavUtil.concat(clazz.getSimpleName().toUpperCase(),DAV_DATA_DIR,"SLICE1"),
                OkWebDavUtil.concat(clazz.getSimpleName().toUpperCase(),DAV_DATA_DIR,"SLICE2")
        };
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

    @Override
    public String[] getAllStoragePaths(String type) {
        return new String[]{OkWebDavUtil.concat(clazz.getSimpleName().toUpperCase(),DAV_DATA_DIR)};
    }
}
