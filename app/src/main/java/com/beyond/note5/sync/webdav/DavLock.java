package com.beyond.note5.sync.webdav;

import com.alibaba.fastjson.JSONObject;
import com.beyond.note5.utils.OkWebDavUtil;

import java.util.Date;

public class DavLock implements Lock{

    private String url;

    public DavLock(String url) {
        this.url = url;
    }

    public boolean tryLock() {
        if (isLocked()) {
            return false;
        }
        return OkWebDavUtil.upload(url, "");
    }

    @Override
    public boolean tryLock(Long time) {
        if (isLocked()) {
            String json = OkWebDavUtil.download(url);
            LockTimeUnit lockTimeUnit = JSONObject.parseObject(json, LockTimeUnit.class);
            if (lockTimeUnit!=null && lockTimeUnit.expired()){
                String lockJson = JSONObject.toJSONString(new LockTimeUnit(new Date(), time));
                return OkWebDavUtil.upload(url, lockJson);
            }
            return false;
        }
        String json = JSONObject.toJSONString(new LockTimeUnit(new Date(), time));
        return OkWebDavUtil.upload(url, json);
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

    private static class LockTimeUnit{
        private Date lastLockTime;
        private Long lockPeriod;

        LockTimeUnit(Date lastLockTime, Long lockPeriod) {
            this.lastLockTime = lastLockTime;
            this.lockPeriod = lockPeriod;
        }

        public Date getLastLockTime() {
            return lastLockTime;
        }

        public void setLastLockTime(Date lastLockTime) {
            this.lastLockTime = lastLockTime;
        }

        public Long getLockPeriod() {
            return lockPeriod;
        }

        public void setLockPeriod(Long lockPeriod) {
            this.lockPeriod = lockPeriod;
        }

        boolean expired() {
            return System.currentTimeMillis() - lastLockTime.getTime() > lockPeriod;
        }
    }
}
