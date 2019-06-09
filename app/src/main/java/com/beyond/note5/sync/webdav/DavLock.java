package com.beyond.note5.sync.webdav;

import com.beyond.note5.utils.OkWebDavUtil;

public class DavLock implements Lock{

    private String url;

    public DavLock(String url) {
        this.url = url;
    }

    public boolean tryLock() {
        if (isLocked()) {
            return false;
        }
        return OkWebDavUtil.upload(url, "");
    }

    public boolean isLocked() {
        return OkWebDavUtil.isFileExist(url);
    }

    public boolean release() {
        try {
            OkWebDavUtil.deleteFile(url);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
