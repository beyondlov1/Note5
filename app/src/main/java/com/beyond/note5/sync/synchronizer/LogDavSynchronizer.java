package com.beyond.note5.sync.synchronizer;

import com.beyond.note5.sync.model.bean.SyncLogInfo;
import com.beyond.note5.sync.model.impl.LogDavModelImpl;
import com.beyond.note5.sync.model.LogSqlModel;
import com.beyond.note5.sync.Synchronizer;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class LogDavSynchronizer implements Synchronizer<SyncLogInfo> {

    private LogSqlModel local;

    private LogDavModelImpl remote;

    public LogDavSynchronizer(LogSqlModel local, LogDavModelImpl remote) {
        this.local = local;
        this.remote = remote;
    }

    @Override
    public boolean sync() throws Exception {

        List<SyncLogInfo> localData = local.getAll();
        List<SyncLogInfo> remoteData = remote.getAll();
        List<SyncLogInfo> localModified = ListUtils.subtract(localData, remoteData);
        List<SyncLogInfo> remoteModified = ListUtils.subtract(remoteData, localData);
        for (SyncLogInfo syncLogInfo : remoteModified) {
            boolean found = false;
            for (SyncLogInfo logInfo : localModified) {
                if (StringUtils.equals(logInfo.getDocumentId(),syncLogInfo.getDocumentId())){
                    localData.add(logInfo.getOperationTime().after(syncLogInfo.getOperationTime())?logInfo:syncLogInfo);
                    found = true;
                }
            }
            if (!found){
                localData.add(syncLogInfo);
            }
        }
        local.saveAll(localData);
        remote.saveAll(localData);
        return true;
    }

}
