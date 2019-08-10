package com.beyond.note5.sync.datasource.sql;


import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.datasource.MultiDataSource;
import com.beyond.note5.sync.datasource.entity.SyncStamp;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class NoteMultiSqlDataSource extends NoteSqlDataSource implements MultiDataSource<Note> {

    private String chosenKey;

    private Map<String, SyncStamp> syncStamps = new LinkedHashMap<>();

    @Override
    public Class<Note> clazz() {
        return Note.class;
    }

    @Override
    public void setChosenKey(String key) {
        chosenKey = key;
    }

    @Override
    public String getChosenKey() {
        return chosenKey;
    }

    @Override
    public void initLastSyncStamps() throws IOException {
        syncStamps = baseSyncStampModel.findAllConnectMe();
    }

    @Override
    public Map<String, SyncStamp> getSyncStampsCache() {
        return syncStamps;
    }

    @Override
    public void setSyncStampsCache(Map<String, SyncStamp> syncStamps) {
        this.syncStamps = syncStamps;
    }
}
