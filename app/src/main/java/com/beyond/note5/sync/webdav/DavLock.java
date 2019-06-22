package com.beyond.note5.sync.webdav;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.beyond.note5.sync.webdav.client.DavClient;

import java.io.IOException;
import java.util.Date;

public class DavLock implements Lock {

    private String url;

    private DavClient client;

    public DavLock(DavClient client, String url) {
        this.url = url;
        this.client = client;
    }

    public boolean tryLock() {
        if (isLocked()) {
            return false;
        }
        try {
            client.put(url, "");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean tryLock(Long time) {
        try {
            if (isLocked()) {
                return false;
            }
            String json = JSONObject.toJSONString(new LockTimeUnit(new Date(), time));
            client.put(url, json);
            return true;
        }catch (Exception e){
            Log.e(getClass().getSimpleName(),"tryLock fail",e);
            return false;
        }
    }

    public boolean isLocked() {
        try {
            if (client.exists(url)){
                String json = client.get(url);
                LockTimeUnit lockTimeUnit = JSONObject.parseObject(json, LockTimeUnit.class);
                return lockTimeUnit == null || !lockTimeUnit.expired();
            }else {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }

    public boolean release() {
        try {
            client.delete(url);
            return true;
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(),"release fail",e);
            return false;
        }
    }

    private static class LockTimeUnit {
        private Date lastLockTime;
        private Long lockPeriod;

        public LockTimeUnit() {
        }

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
