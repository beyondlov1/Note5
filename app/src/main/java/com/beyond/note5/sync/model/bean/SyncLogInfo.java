package com.beyond.note5.sync.model.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;
import java.util.Objects;

@Entity
public class SyncLogInfo {
    public static final String UPDATE = "update";
    public static final String ADD = "add";
    @Id
    private String id;
    private String refPath;
    private String refServer;
    private String operation;
    private Date operationTime;
    private String source;

    @Generated(hash = 1282716146)
    public SyncLogInfo(String id, String refPath, String refServer,
            String operation, Date operationTime, String source) {
        this.id = id;
        this.refPath = refPath;
        this.refServer = refServer;
        this.operation = operation;
        this.operationTime = operationTime;
        this.source = source;
    }
    @Generated(hash = 496388046)
    public SyncLogInfo() {
    }

    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getRefPath() {
        return this.refPath;
    }
    public void setRefPath(String refPath) {
        this.refPath = refPath;
    }
    public String getRefServer() {
        return this.refServer;
    }
    public void setRefServer(String refServer) {
        this.refServer = refServer;
    }
    public String getOperation() {
        return this.operation;
    }
    public void setOperation(String operation) {
        this.operation = operation;
    }
    public Date getOperationTime() {
        return this.operationTime;
    }
    public void setOperationTime(Date operationTime) {
        this.operationTime = operationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SyncLogInfo that = (SyncLogInfo) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(refPath, that.refPath) &&
                Objects.equals(refServer, that.refServer) &&
                Objects.equals(operation, that.operation) &&
                Objects.equals(operationTime, that.operationTime);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, refPath, refServer, operation, operationTime);
    }
    public String getSource() {
        return this.source;
    }
    public void setSource(String source) {
        this.source = source;
    }
}
