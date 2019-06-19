package com.beyond.note5.sync.datasource.note;

import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.datasource.SingleDavDataSource;
import com.beyond.note5.sync.webdav.client.DavClient;

public class NoteSingleDavDataSource extends SingleDavDataSource<Note> {

    public NoteSingleDavDataSource(DavClient client, String url) {
        super(client, url);
    }

    @Override
    public Class<Note> clazz() {
        return Note.class;
    }

    @Override
    public String[] getNodes() {
        return new String[]{url};
    }

    @Override
    public String[] getPaths() {
        return new String[]{""};
    }

    @Override
    public String getNode(Note note) {
        return url;
    }

    @Override
    public String getPath(Note note) {
        return "";
    }
}
