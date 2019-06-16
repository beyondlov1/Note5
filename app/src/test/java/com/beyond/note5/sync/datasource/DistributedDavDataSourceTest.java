package com.beyond.note5.sync.datasource;

import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.datasource.note.NoteDistributedDavDataSource;
import com.beyond.note5.sync.webdav.CommonTest;
import com.beyond.note5.sync.webdav.client.DavClient;
import com.beyond.note5.sync.webdav.client.SardineDavClient;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.beyond.note5.sync.webdav.CommonTest.getExampleNotes;
import static com.beyond.note5.sync.webdav.CommonTest.getRootUrl;

public class DistributedDavDataSourceTest {

    DavClient client = new SardineDavClient(CommonTest.getUsername(),CommonTest.getPassword());

    @Test
    public void add() throws IOException {
        DistributedDavDataSource<Note> distributedDavDataSource = new DistributedDavDataSource<Note>(client,CommonTest.getExecutorService(),
                getRootUrl()) {

            @Override
            public Class<Note> clazz() {
                return Note.class;
            }
        };

        distributedDavDataSource.add(getExampleNotes().get(0));
        distributedDavDataSource.add(getExampleNotes().get(1));
    }

    @Test
    public void distributeAdd() throws IOException {

        DistributedDavDataSource<Note> distributedDavDataSource = new DistributedDavDataSource<Note>(client,CommonTest.getExecutorService(),
                getRootUrl(),getRootUrl()+"NoteCloud2") {
            @Override
            public Class<Note> clazz() {
                return Note.class;
            }
        };

        distributedDavDataSource.add(getExampleNotes().get(0));
        distributedDavDataSource.add(getExampleNotes().get(1));
        distributedDavDataSource.add(getExampleNotes().get(2));
        distributedDavDataSource.add(getExampleNotes().get(3));
    }

    @Test
    public void delete() throws IOException {

        DistributedDavDataSource<Note> distributedDavDataSource = new DistributedDavDataSource<Note>(client,CommonTest.getExecutorService(),
                getRootUrl(),getRootUrl()+"NoteCloud2") {
            @Override
            public Class<Note> clazz() {
                return Note.class;
            }
        };
        distributedDavDataSource.delete(getExampleNotes().get(0));
    }

    @Test
    public void update() throws IOException {
        DistributedDavDataSource<Note> distributedDavDataSource = new DistributedDavDataSource<Note>(client,CommonTest.getExecutorService(),
                getRootUrl(),getRootUrl()+"NoteCloud2") {
            @Override
            public Class<Note> clazz() {
                return Note.class;
            }
        };
        Note note = getExampleNotes().get(0);
        note.setContent("updated1");
        distributedDavDataSource.update(note);
    }

    @Test
    public void select() throws IOException {
        DistributedDavDataSource<Note> distributedDavDataSource = new DistributedDavDataSource<Note>(client,CommonTest.getExecutorService(),
                getRootUrl(),getRootUrl()+"NoteCloud2") {
            @Override
            public Class<Note> clazz() {
                return Note.class;
            }
        };
        Note note = getExampleNotes().get(0);
        Note selected = distributedDavDataSource.select(note);
        assert selected.getId().equals(note.getId());
    }

    @Test
    public void selectAll() throws IOException {
        DistributedDavDataSource<Note> distributedDavDataSource = new DistributedDavDataSource<Note>(client,CommonTest.getExecutorService(),
                getRootUrl(),getRootUrl()+"NoteCloud2") {
            @Override
            public Class<Note> clazz() {
                return Note.class;
            }
        };

        List<Note> list = distributedDavDataSource.selectAll();
        for (Note note : list) {
            System.out.println(note);
        }
    }


    @Test
    public void cover() {
    }

    @Test
    public void clazz(){
        DistributedDavDataSource distributedDavDataSource = new NoteDistributedDavDataSource(client,CommonTest.getExecutorService(),
                getRootUrl(),getRootUrl()+"NoteCloud2");
        Class clazz = distributedDavDataSource.clazz();
        System.out.println(clazz);
    }
}