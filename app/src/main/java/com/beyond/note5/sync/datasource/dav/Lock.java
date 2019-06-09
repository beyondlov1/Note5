package com.beyond.note5.sync.datasource.dav;

public interface Lock {

    boolean tryLock();

    boolean isLocked();

    boolean release();
}
