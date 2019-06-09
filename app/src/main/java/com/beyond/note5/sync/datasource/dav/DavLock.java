package com.beyond.note5.sync.datasource.dav;

import com.beyond.note5.utils.OkWebDavUtil;
import com.beyond.note5.utils.PreferenceUtil;

import static com.beyond.note5.MyApplication.VIRTUAL_USER_ID;

public class DavLock implements Lock{

    private String url;

    public DavLock(String url) {
        this.url = url;
    }

    public boolean tryLock() {
        if (isLocked()) {
            return false;
        }
        return OkWebDavUtil.upload(url, PreferenceUtil.getString(VIRTUAL_USER_ID));
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

    public static void main(String[] args) {
        DavLock davLock = new DavLock("https://dav.jianguoyun.com/dav/NoteClould2/test.lock");
        boolean locked = davLock.isLocked();
        System.out.println(locked);
        System.out.println();

        davLock.release();
    }
}
