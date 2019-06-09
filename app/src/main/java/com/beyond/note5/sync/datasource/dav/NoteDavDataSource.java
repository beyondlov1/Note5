package com.beyond.note5.sync.datasource.dav;

import com.beyond.note5.bean.Note;

public class NoteDavDataSource extends DavDataSource<Note> {
    public NoteDavDataSource(String url) {
        super(url);
    }

    @Override
    public Class clazz() {
        return Note.class;
    }
}
