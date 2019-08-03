package com.beyond.note5.sync;

import com.beyond.note5.sync.model.entity.TraceLog;
import com.beyond.note5.sync.model.impl.DavLogModelImpl;
import com.beyond.note5.sync.datasource.sql.model.SqlLogModel;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Deprecated
public class LogDavSynchronizer implements Synchronizer<TraceLog> {

    private SqlLogModel local;

    private DavLogModelImpl remote;

    public LogDavSynchronizer(SqlLogModel local, DavLogModelImpl remote) {
        this.local = local;
        this.remote = remote;
    }

    @Override
    public boolean sync() throws Exception {

        List<TraceLog> localData = local.getAll();
        List<TraceLog> remoteData = remote.getAll();
        List<TraceLog> localModified = ListUtils.subtract(localData, remoteData);
        List<TraceLog> remoteModified = ListUtils.subtract(remoteData, localData);
        for (TraceLog traceLog : remoteModified) {
            boolean found = false;
            for (TraceLog logInfo : localModified) {
                if (StringUtils.equals(logInfo.getDocumentId(), traceLog.getDocumentId())){
                    localData.add(logInfo.getOperationTime().after(traceLog.getOperationTime())?logInfo: traceLog);
                    found = true;
                }
            }
            if (!found){
                localData.add(traceLog);
            }
        }
        local.saveAll(localData);
        remote.saveAll(localData);
        return true;
    }

}
