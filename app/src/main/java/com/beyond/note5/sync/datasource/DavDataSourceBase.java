package com.beyond.note5.sync.datasource;


import com.beyond.note5.sync.webdav.Lock;

public abstract class DavDataSourceBase<T> implements DavDataSource<T> {

    protected abstract Lock getLock();

    @Override
    public boolean tryLock(Long time) {
        return getLock().tryLock(time);
    }

    @Override
    public boolean isLocked() {
        return getLock().isLocked();
    }

    @Override
    public boolean tryLock() {
        return getLock().tryLock();
    }

    @Override
    public boolean release() {
        return getLock().release();
    }

}
