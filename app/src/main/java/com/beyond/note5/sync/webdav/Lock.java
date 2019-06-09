package com.beyond.note5.sync.webdav;

public interface Lock {

    boolean tryLock();

    boolean isLocked();

    boolean release();
}
