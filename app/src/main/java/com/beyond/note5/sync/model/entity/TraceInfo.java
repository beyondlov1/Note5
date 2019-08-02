package com.beyond.note5.sync.model.entity;

import java.util.Date;

public class TraceInfo {
    public static final TraceInfo ZERO = create(new Date(0), new Date(0), new Date(0));
    private Date lastModifyTime;
    private Date lastSyncTimeStart;
    private Date lastSyncTimeEnd;

    public static TraceInfo create(Date lastModifyTime, Date lastSyncTime){
        TraceInfo traceInfo = new TraceInfo();
        traceInfo.setLastModifyTime(lastModifyTime == null?new Date(0):lastModifyTime);
        traceInfo.setLastSyncTimeEnd(lastSyncTime == null?new Date(0):lastSyncTime);
        return traceInfo;
    }

    public static TraceInfo create(Date lastModifyTime, Date lastSyncTimeStart, Date lastSyncTimeEnd){
        TraceInfo traceInfo = new TraceInfo();
        traceInfo.setLastModifyTime(lastModifyTime == null?new Date(0):lastModifyTime);
        traceInfo.setLastSyncTimeStart(lastSyncTimeStart == null?new Date(0):lastSyncTimeStart);
        traceInfo.setLastSyncTimeEnd(lastSyncTimeEnd == null?new Date(0):lastSyncTimeEnd);
        return traceInfo;
    }

    public Date getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(Date lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public Date getLastSyncTimeEnd() {
        return lastSyncTimeEnd;
    }

    public void setLastSyncTimeEnd(Date lastSyncTimeEnd) {
        this.lastSyncTimeEnd = lastSyncTimeEnd;
    }

    public Date getLastSyncTimeStart() {
        return lastSyncTimeStart;
    }

    public void setLastSyncTimeStart(Date lastSyncTimeStart) {
        this.lastSyncTimeStart = lastSyncTimeStart;
    }
}
