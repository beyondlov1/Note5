package com.beyond.note5.sync.synchronizer;

import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.DistributedDavDataSource;
import com.beyond.note5.sync.datasource.note.NoteDistributedDavDataSource;
import com.beyond.note5.sync.webdav.CommonTest;
import com.beyond.note5.sync.webdav.client.DavClient;
import com.beyond.note5.sync.webdav.client.SardineDavClient;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NoteSynchronizerTest {

    @Test
    public void sync() {

        Note note = Note.newInstance();
        Note note1 = Note.newInstance();
        Note note2 = Note.newInstance();
        Note note3 = Note.newInstance();
        Note note4 = Note.newInstance();
        Note note5 = Note.newInstance();
        Note note6 = Note.newInstance();

        note.setId("0");
        note1.setId("1");
        note2.setId("2");
        note3.setId("3");
        note4.setId("4");
        note5.setId("5");
        note6.setId("6");

        note.setLastModifyTime(setTime(2019, 1, 12));
        note1.setLastModifyTime(setTime(2018, 1, 12));
        note2.setLastModifyTime(setTime(2019, 1, 1));
        note3.setLastModifyTime(setTime(2019, 1, 12));
        note4.setLastModifyTime(setTime(2017, 1, 12));
        note5.setLastModifyTime(setTime(2020, 1, 12));
        note6.setLastModifyTime(setTime(2020, 1, 12));

        note.setTitle("note");
        note1.setTitle("note1");
        note2.setTitle("note2");
        note3.setTitle("note3");
        note4.setTitle("note4");
        note5.setTitle("note5");
        note6.setTitle("note6");

        List<Attachment> attachments = new ArrayList<>();
        note.setAttachments(attachments);
        note1.setAttachments(attachments);
        note2.setAttachments(attachments);
        note3.setAttachments(attachments);
        note4.setAttachments(attachments);
        note5.setAttachments(attachments);
        note6.setAttachments(attachments);


        List<Note> localList = new ArrayList<>();
        localList.add(note);  // add
        localList.add(note1);
//        localList.add(note2); //delete

        Note note3Clone = ObjectUtils.clone(note3);
        note3Clone.setLastModifyTime(setTime(2021, 10, 4));
        note3Clone.setVersion(note3.getVersion() + 1);
        localList.add(note3Clone); //update
        localList.add(note4);
        localList.add(note6);

        List<Note> remoteList = new ArrayList<>();
        remoteList.add(note1);
        remoteList.add(note2);
        Note note3CloneR = ObjectUtils.clone(note3);
        note3CloneR.setLastModifyTime(setTime(2021, 10, 4));
        note3CloneR.setVersion(note3.getVersion() + 2);
        remoteList.add(note3CloneR); //remote update sameone
//        remoteList.add(note4);  // remote delete
        remoteList.add(note5); // remote add
        Note note6Clone = ObjectUtils.clone(note6);
        note6Clone.setLastModifyTime(setTime(2021, 10, 4));
        note6Clone.setVersion(note6.getVersion() + 1);
        remoteList.add(note6Clone); // remote update


        SynchronizerSupport<Note> synchronizer = new SingleNoteSynchronizer();
        List<Note> mergedData = synchronizer.getLocalAddedData(localList, remoteList);
        System.out.println("getLocalAddedData:");
        for (Note mergedDatum : mergedData) {
            System.out.println(mergedDatum);
        }
        System.out.println();

        mergedData = synchronizer.getLocalUpdatedData(localList, remoteList);
        System.out.println("getLocalUpdatedData:");
        for (Note mergedDatum : mergedData) {
            System.out.println(mergedDatum);
        }
        System.out.println();


        mergedData = synchronizer.getLocalDeletedData(localList, remoteList);
        System.out.println("getLocalDeletedData:");
        for (Note mergedDatum : mergedData) {
            System.out.println(mergedDatum);
        }
        System.out.println();


        mergedData = synchronizer.getRemoteAddedData(localList, remoteList);
        System.out.println("getRemoteAddedData:");
        for (Note mergedDatum : mergedData) {
            System.out.println(mergedDatum);
        }
        System.out.println();

        mergedData = synchronizer.getRemoteUpdatedData(localList, remoteList);
        System.out.println("getRemoteUpdatedData:");
        for (Note mergedDatum : mergedData) {
            System.out.println(mergedDatum);
        }
        System.out.println();

        mergedData = synchronizer.getRemoteDeleteData(localList, remoteList);
        System.out.println("getRemoteDeleteData:");
        for (Note mergedDatum : mergedData) {
            System.out.println(mergedDatum);
        }
        System.out.println();


        DavClient davClient = new SardineDavClient(CommonTest.getUsername(),CommonTest.getPassword());
        DistributedDavDataSource<Note> remoteDataSource = new NoteDistributedDavDataSource(davClient,
                CommonTest.getExecutorService(),
                "https://dav.jianguoyun.com/dav/test");
        DataSource<Note> localDataSource = new DataSource<Note>() {

            List<Note> notes = localList;

            @Override
            public void add(Note note) {
                notes.add(note);
            }

            @Override
            public void delete(Note note) {
                notes.remove(getIndex(note));
            }

            @Override
            public void update(Note note) {
                notes.set(getIndex(note), note);
            }

            @Override
            public Note select(Note note) {
                return notes.get(getIndex(note));
            }

            @Override
            public Note selectById(String id) throws IOException {
                Note note = new Note();
                note.setId(id);
                return notes.get(getIndex(note));
            }

            @Override
            public List<Note> selectAll() throws IOException {
                return notes;
            }

            @Override
            public void cover(List<Note> all) throws IOException {
                notes.clear();
                notes.addAll(all);
            }

            @Override
            public Class clazz() {
                return Note.class;
            }

            private int getIndex(Note n) {
                int index = 0;
                for (Note note : notes) {
                    if (StringUtils.equals(n.getId(), note.getId())) {
                        return index;
                    }
                    index++;
                }

                return -1;
            }
        };

        try {
            synchronizer.sync(localDataSource, remoteDataSource);
            System.out.println();
            System.out.println();

            for (Note n : localDataSource.selectAll()) {
                System.out.println(n.getTitle());
            }

            for (Note n : remoteDataSource.selectAll()) {
                System.out.println(n.getTitle());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private Date setTime(int year, int month, int day) {
        Calendar instance = Calendar.getInstance();
        instance.set(year, month, day);
        return instance.getTime();
    }

}