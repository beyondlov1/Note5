package com.beyond.note5.sync.datasource.note;

import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.datasource.DistributedDavDataSource;
import com.beyond.note5.sync.webdav.client.DavClient;

import java.util.concurrent.ExecutorService;

public class NoteDistributedDavDataSource extends DistributedDavDataSource<Note> {

    public NoteDistributedDavDataSource(DavClient client, String... rootUrls) {
        super(client, rootUrls);
    }

    public NoteDistributedDavDataSource(DavClient client, ExecutorService executorService, String... rootUrls) {
        super(client, executorService, rootUrls);
    }

    @Override
    public Class<Note> clazz() {
        return Note.class;
    }
}
