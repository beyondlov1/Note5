package com.beyond.note5.sync.datasource;

import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.webdav.CommonTest;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static com.beyond.note5.sync.webdav.CommonTest.getDistributedDavDataSource;
import static com.beyond.note5.sync.webdav.CommonTest.getDistributedDavDataSource2;
import static com.beyond.note5.sync.webdav.CommonTest.getExampleNotes;
import static com.beyond.note5.sync.webdav.CommonTest.getExecutorService;
import static com.beyond.note5.utils.AsyncUtil.computeAllAsyn;

public class DistributedDavDataSourceTest {

    DavDataSource<Note> distributedDavDataSource = CommonTest.getDistributedDavDataSource();

    @Test
    public void add() throws IOException {

        distributedDavDataSource.add(getExampleNotes().get(0));
        distributedDavDataSource.add(getExampleNotes().get(1));
    }

    @Test
    public void distributeAdd() throws IOException {

        distributedDavDataSource.add(getExampleNotes().get(0));
        distributedDavDataSource.add(getExampleNotes().get(1));
        distributedDavDataSource.add(getExampleNotes().get(2));
        distributedDavDataSource.add(getExampleNotes().get(3));
    }

    @Test
    public void delete() throws IOException {
        distributedDavDataSource.delete(getExampleNotes().get(0));
    }

    @Test
    public void update() throws IOException {
        Note note = getExampleNotes().get(0);
        note.setContent("updated1");
        distributedDavDataSource.update(note);
    }

    @Test
    public void select() throws IOException {
        Note note = getExampleNotes().get(2);
        Note selected = distributedDavDataSource.select(note);
        assert selected.getId().equals(note.getId());
    }


    @Test
    public void selectAll() throws IOException, ExecutionException, InterruptedException {
        List<Note> list = distributedDavDataSource.selectAll();
        for (Note note : list) {
            System.out.println(note);
        }
    }

    @Test
    public void selectAll2() throws IOException, ExecutionException, InterruptedException {
        List<DataSource<Note>> subDataSources = new ArrayList<>();
        DavDataSource<Note> distributedDavDataSource1 = getDistributedDavDataSource();
        DavDataSource<Note> distributedDavDataSource2 = getDistributedDavDataSource2();
        subDataSources.add(distributedDavDataSource1);
        subDataSources.add(distributedDavDataSource2);

        List<Callable<List<Note>>> callables = new ArrayList<>();
        for (DataSource<Note> subDataSource : subDataSources) {
            callables.add(new Callable<List<Note>>() {
                @Override
                public List<Note> call() throws Exception {
                    return subDataSource.selectAll();
                }
            });
        }
        List<Note> list = computeAllAsyn(getExecutorService(), callables);
        System.out.println(list);
    }


    @Test
    public void cover() {
    }

}