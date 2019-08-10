package com.beyond.note5.sync.builder;

import android.support.annotation.NonNull;

import com.beyond.note5.bean.Account;
import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.datasource.MultiDataSource;
import com.beyond.note5.sync.datasource.dav.DavDataSourceProperty;
import com.beyond.note5.sync.datasource.dav.DefaultMultiDavDataSource;
import com.beyond.note5.sync.datasource.sql.NoteMultiSqlDataSource;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author: beyond
 * @date: 2019/8/9
 */

public class NoteMultiSynchronizerBuilder extends AbstractMultiSynchronizerBuilder<Note> {

    public NoteMultiSynchronizerBuilder(List<Account> accounts) {
        super(accounts);
    }

    @NonNull
    @Override
    protected MultiDataSource<Note> getMultiSqlDataSource() {
        return new NoteMultiSqlDataSource();
    }

    @NonNull
    @Override
    protected MultiDataSource<Note> getMultiDavDataSource(ExecutorService executorService, DavDataSourceProperty property) {
        return new DefaultMultiDavDataSource<>(property, Note.class, executorService);
    }
}
