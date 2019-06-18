package com.beyond.note5.sync.webdav;

public interface Lock {

    boolean tryLock();

    boolean tryLock(Long time);

    boolean isLocked();

    boolean release();
}
