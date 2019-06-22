package com.beyond.note5.sync.model.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;

@Entity
public class SyncInfo {

    @Id
    private String id;
    private String localKey;
    private String remoteKey;
    private Date lastSyncTime;

    @Generated(hash = 525848672)
    public SyncInfo(String id, String localKey, String remoteKey,
            Date lastSyncTime) {
        this.id = id;
        this.localKey = localKey;
        this.remoteKey = remoteKey;
        this.lastSyncTime = lastSyncTime;
    }

    @Generated(hash = 57154307)
    public SyncInfo() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocalKey() {
        return localKey;
    }

    public void setLocalKey(String localKey) {
        this.localKey = localKey;
    }

    public String getRemoteKey() {
        return remoteKey;
    }

    public void setRemoteKey(String remoteKey) {
        this.remoteKey = remoteKey;
    }

    public Date getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(Date lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }
}
