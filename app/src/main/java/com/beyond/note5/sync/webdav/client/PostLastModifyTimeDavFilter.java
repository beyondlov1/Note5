package com.beyond.note5.sync.webdav.client;

import android.support.annotation.NonNull;

import com.thegrizzlylabs.sardineandroid.DavResource;

import java.util.Date;

public class PostLastModifyTimeDavFilter implements DavFilter {

    private Date date;

    public PostLastModifyTimeDavFilter(Date date) {
        this.date = date;
    }

    @Override
    public boolean filter(@NonNull DavResource davResource) {
        return davResource.getModified() == null || !davResource.getModified().after(date);
    }
}
