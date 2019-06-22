package com.beyond.note5.sync.datasource.note;

import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.datasource.SingleDavDataSource;
import com.beyond.note5.sync.webdav.client.DavClient;

import java.io.IOException;
import java.util.Date;

@Deprecated
public class NoteSingleDavDataSource extends SingleDavDataSource<Note> {

    public NoteSingleDavDataSource(DavClient client, String url) {
        super(client, url);
    }

    @Override
    public Class<Note> clazz() {
        return Note.class;
    }

    @Override
    public String getKey() {
        return url;
    }

    public Date getLastSyncTime(String syncTargetKey) throws IOException {
        return null;
    }

    public void setLastSyncTime(String syncTargetKey, Date date) throws IOException {

    }


    public String getServer(Note note) {
        return null;
    }

    public String[] getPaths(Note note) {
        return new String[0];
    }

    @Override
    public String getServer() {
        return null;
    }

    @Override
    public String[] getPaths() {
        return new String[0];
    }

    @Override
    public String getPath(Note note) {
        return null;
    }

    @Override
    public DavClient getClient() {
        return null;
    }

    @Override
    public Date getLastSyncTime() throws IOException {
        return null;
    }

    @Override
    public void setLastSyncTime(Date date) throws IOException {

    }
}
