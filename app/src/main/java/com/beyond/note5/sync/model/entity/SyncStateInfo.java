package com.beyond.note5.sync.model.entity;

import com.beyond.note5.utils.IDUtil;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class SyncStateInfo {

    public static final Integer NONE = 0;
    public static final Integer SUCCESS = 1;
    public static final Integer FAIL = 2;

    @Id
    private String id;
    private String documentId;
    private String local;
    private String server;
    private String type;
    private Integer state;

    @Generated(hash = 132018600)
    public SyncStateInfo(String id, String documentId, String local, String server,
            String type, Integer state) {
        this.id = id;
        this.documentId = documentId;
        this.local = local;
        this.server = server;
        this.type = type;
        this.state = state;
    }

    @Generated(hash = 513884121)
    public SyncStateInfo() {
    }

    public static SyncStateInfo create(){
        SyncStateInfo syncStateInfo = new SyncStateInfo();
        syncStateInfo.setId(IDUtil.uuid());
        syncStateInfo.setState(SUCCESS);
        return syncStateInfo;
    }

    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getDocumentId() {
        return this.documentId;
    }
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
    public String getServer() {
        return this.server;
    }
    public void setServer(String server) {
        this.server = server;
    }
    public Integer getState() {
        return this.state;
    }
    public void setState(Integer state) {
        this.state = state;
    }
    public String getLocal() {
        return this.local;
    }
    public void setLocal(String local) {
        this.local = local;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }


}
