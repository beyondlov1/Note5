package com.beyond.note5.sync.datasource.note;

import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.datasource.DavDataSource;
import com.beyond.note5.sync.datasource.DavDataSourceComposite;

import java.util.concurrent.ExecutorService;

public class NoteDavDataSourceComposite extends DavDataSourceComposite<Note> {

    public NoteDavDataSourceComposite(DavDataSource<Note>... subDataSources) {
        super(subDataSources);
    }

    public NoteDavDataSourceComposite(ExecutorService executorService, DavDataSource<Note>... subDataSources) {
        super(executorService, subDataSources);
    }

    @Override
    public Class<Note> clazz() {
        return Note.class;
    }
}
