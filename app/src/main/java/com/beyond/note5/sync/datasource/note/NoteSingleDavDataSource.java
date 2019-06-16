package com.beyond.note5.sync.datasource.note;

import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.datasource.SingleDavDataSource;

public class NoteSingleDavDataSource extends SingleDavDataSource<Note> {
    public NoteSingleDavDataSource(String url) {
        super(url);
    }

    @Override
    public Class clazz() {
        return Note.class;
    }
}
