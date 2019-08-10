package com.beyond.note5.sync.webdav.client;

import android.support.annotation.NonNull;

import com.thegrizzlylabs.sardineandroid.DavResource;

/**
 * @author: beyond
 * @date: 2019/8/9
 */

public class PrefixDavFilter implements DavFilter {
    private final String prefix;

    public PrefixDavFilter(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean filter(@NonNull DavResource davResource) {
        return !davResource.getName().startsWith(prefix);
    }
}
