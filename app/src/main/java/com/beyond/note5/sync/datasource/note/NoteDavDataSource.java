package com.beyond.note5.sync.datasource.note;

import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.datasource.DavDataSource;

public class NoteDavDataSource extends DavDataSource<Note> {
    public NoteDavDataSource(String url) {
        super(url);
    }

    @Override
    public Class clazz() {
        return Note.class;
    }
}
