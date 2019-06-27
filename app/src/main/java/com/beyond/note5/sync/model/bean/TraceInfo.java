package com.beyond.note5.sync.model.bean;

import java.util.Date;

public class TraceInfo {
    public static final TraceInfo ZERO = create(new Date(0), new Date(0));
    private Date lastModifyTime;
    private Date lastSyncTime;

    public static TraceInfo create(Date lastModifyTime, Date lastSyncTime){
        TraceInfo traceInfo = new TraceInfo();
        traceInfo.setLastModifyTime(lastModifyTime == null?new Date(0):lastModifyTime);
        traceInfo.setLastSyncTime(lastSyncTime == null?new Date(0):lastSyncTime);
        return traceInfo;
    }
    
    public Date getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(Date lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public Date getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(Date lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }
}
