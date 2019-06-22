package com.beyond.note5.sync.datasource;

import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.webdav.CommonTest;
import com.beyond.note5.sync.webdav.client.DavClient;
import com.beyond.note5.sync.webdav.client.SardineDavClient;

import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.beyond.note5.sync.webdav.CommonTest.getDistributedDavDataSource;
import static com.beyond.note5.sync.webdav.CommonTest.getExampleNotes;

public class DavDataSourceCompositeTest {

    DavClient nutStoreClient = new SardineDavClient(CommonTest.getUsername(),CommonTest.getPassword());
    DavClient teraClient = new SardineDavClient(CommonTest.getUsername(),CommonTest.getPassword());


    DavDataSource<Note> davDataSourceComposite = getDistributedDavDataSource();

    @Test
    public void add() throws IOException {
        davDataSourceComposite.add(getExampleNotes().get(0));
        davDataSourceComposite.add(getExampleNotes().get(1));
        davDataSourceComposite.add(getExampleNotes().get(2));
        davDataSourceComposite.add(getExampleNotes().get(3));
        davDataSourceComposite.add(getExampleNotes().get(4));
    }

    @Test
    public void delete() throws IOException {
        davDataSourceComposite.delete(getExampleNotes().get(0));
    }

    @Test
    public void update() throws IOException {
        Note note = getExampleNotes().get(0);
        note.setContent("updated lals");
        davDataSourceComposite.update(note);
    }

    @Test
    public void select() throws IOException {
        Note select = davDataSourceComposite.select(getExampleNotes().get(0));
        System.out.println(select);
    }

    @Test
    public void selectById() throws IOException {
        davDataSourceComposite.selectById(getExampleNotes().get(0).getId());
    }

    @Test
    public void selectAll() throws IOException, ExecutionException, InterruptedException {
        List<Note> list = davDataSourceComposite.selectAll();
        for (Note note : list) {
            System.out.println(note);
        }
    }

    @Test
    public void cover() throws InterruptedException, ExecutionException, IOException {
        davDataSourceComposite.cover(getExampleNotes());
    }
}