package com.beyond.note5.sync.webdav;

import com.beyond.note5.sync.webdav.client.DavClient;
import com.beyond.note5.sync.webdav.client.SardineDavClient;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DavLockTest {

    @Test
    public void tryLock() {
        DavClient client = new SardineDavClient("", "");

        DavLock davLock = new DavLock(client, "https://dav.jianguoyun.com/dav/NoteClould2/test.tryLock");

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