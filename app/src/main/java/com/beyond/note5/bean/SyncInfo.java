package com.beyond.note5.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;

@Entity
public class SyncInfo {

    @Id
    private String id;
    private String node;
    private String path;
    private Date lastSyncTime;


    @Generated(hash = 1170332257)
    public SyncInfo(String id, String node, String path, Date lastSyncTime) {
        this.id = id;
        this.node = node;
        this.path = path;
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

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Date getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(Date lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }
}
