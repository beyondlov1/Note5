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
    private String documentId;
    private String refPath;
    private String refServer;
    private String operation;
    private Date operationTime;
    private String source;
    private String type;

    @Generated(hash = 1761960630)
    public SyncLogInfo(String id, String documentId, String refPath, String refServer, String operation,
            Date operationTime, String source, String type) {
        this.id = id;
        this.documentId = documentId;
        this.refPath = refPath;
        this.refServer = refServer;
        this.operation = operation;
        this.operationTime = operationTime;
        this.source = source;
        this.type = type;
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
                Objects.equals(documentId, that.documentId) &&
                Objects.equals(refPath, that.refPath) &&
                Objects.equals(refServer, that.refServer) &&
                Objects.equals(operation, that.operation) &&
                Objects.equals(operationTime, that.operationTime) &&
                Objects.equals(source, that.source) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, documentId, refPath, refServer, operation, operationTime, source, type);
    }

    public String getSource() {
        return this.source;
    }
    public void setSource(String source) {
        this.source = source;
    }
    public String getDocumentId() {
        return this.documentId;
    }
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
    public String getType() {
        return this.type;
    }
    public void setType(String type) {
        this.type = type;
    }
}
