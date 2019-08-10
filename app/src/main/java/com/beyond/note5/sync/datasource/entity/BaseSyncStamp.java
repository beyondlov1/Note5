package com.beyond.note5.sync.datasource.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;

@Entity
public class BaseSyncStamp {

    @Id
    private String id;
    private String localKey;
    private String remoteKey;
    private Date lastModifyTime;
    private Date lastSyncTimeStart;
    private Date lastSyncTime;
    private String type;




    @Generated(hash = 2081333840)
    public BaseSyncStamp(String id, String localKey, String remoteKey,
            Date lastModifyTime, Date lastSyncTimeStart, Date lastSyncTime,
            String type) {
        this.id = id;
        this.localKey = localKey;
        this.remoteKey = remoteKey;
        this.lastModifyTime = lastModifyTime;
        this.lastSyncTimeStart = lastSyncTimeStart;
        this.lastSyncTime = lastSyncTime;
        this.type = type;
    }

    @Generated(hash = 1862814984)
    public BaseSyncStamp() {
    }




    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Date getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(Date lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public Date getLastSyncTimeStart() {
        return this.lastSyncTimeStart;
    }

    public void setLastSyncTimeStart(Date lastSyncTimeStart) {
        this.lastSyncTimeStart = lastSyncTimeStart;
    }
}
