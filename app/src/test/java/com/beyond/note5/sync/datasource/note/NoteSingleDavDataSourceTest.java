package com.beyond.note5.sync.datasource.note;

import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.datasource.SingleDavDataSource;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NoteSingleDavDataSourceTest {

    @Test
    public void selectAll() {
    }

    @Test
    public void cover() throws IOException {
        SingleDavDataSource<Note> singleDavDataSource = new NoteSingleDavDataSource("https://dav.jianguoyun.com/dav/NoteClould2/test");

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


        List<Note> remoteList = new ArrayList<>();
        remoteList.add(note1);
        remoteList.add(note2);
        Note note3CloneR = ObjectUtils.clone(note3);
        note3CloneR.setLastModifyTime(setTime(2021, 10, 4));
        note3CloneR.setVersion(note3.getVersion()+2);
        remoteList.add(note3CloneR); //remote update sameone
//        remoteList.add(note4);  // remote delete
        remoteList.add(note5); // remote add
        Note note6Clone = ObjectUtils.clone(note6);
        note6Clone.setLastModifyTime(setTime(2021, 10, 4));
        note6Clone.setVersion(note6.getVersion()+1);
        remoteList.add(note6Clone); // remote update

        singleDavDataSource.cover(remoteList);
        List<Note> list = singleDavDataSource.selectAll();
        System.out.println(list);
    }

    private static Date setTime(int year, int month, int day) {
        Calendar instance = Calendar.getInstance();
        instance.set(year, month, day);
        return instance.getTime();
    }
}