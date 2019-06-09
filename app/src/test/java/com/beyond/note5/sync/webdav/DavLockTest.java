package com.beyond.note5.sync.webdav;

import org.junit.Test;

import static org.junit.Assert.*;

public class DavLockTest {

    @Test
    public void tryLock() {
        DavLock davLock = new DavLock("https://dav.jianguoyun.com/dav/NoteClould2/test.lock");

        assertFalse(davLock.isLocked());

        assertTrue(davLock.tryLock());

        assertTrue(davLock.isLocked());

        davLock.release();
    }

    @Test
    public void isLocked() {

    }

    @Test
    public void release() {
    }
}