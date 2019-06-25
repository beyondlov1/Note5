package com.beyond.note5.sync.webdav.client;

import android.support.annotation.NonNull;

import com.thegrizzlylabs.sardineandroid.DavResource;

public interface DavFilter {
    /**
     * 返回true为过滤掉， 返回false则不过滤
     * @param davResource
     * @return
     */
    boolean filter(@NonNull DavResource davResource);
}
