package com.beyond.note5.sync.webdav;

import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.webdav.client.DavClient;
import com.beyond.note5.sync.webdav.client.SardineDavClient;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.beyond.note5.utils.StringCompressUtilTest.setTime;

public class CommonTest {
    public static String getRootUrl(){
        return "https://dav.jianguoyun.com/dav/";
    }
    public static List<Note> getExampleNotes(){
        Note note = Note.newInstance();
        note.setId("0");
        note.setLastModifyTime(setTime(2019, 1, 12));
        note.setTitle("note");
        List<Attachment> attachments = new ArrayList<>();
        note.setAttachments(attachments);

        Note note2 = Note.newInstance();
        note2.setId("2");
        note2.setLastModifyTime(setTime(2019, 1, 12));
        note2.setTitle("note2");
        List<Attachment> attachments2 = new ArrayList<>();
        note2.setAttachments(attachments2);

        Note note3 = Note.newInstance();
        note3.setId("3");
        note3.setLastModifyTime(setTime(2019, 1, 12));
        note3.setTitle("note3");
        List<Attachment> attachments3 = new ArrayList<>();
        note3.setAttachments(attachments3);

        Note note4 = Note.newInstance();
        note4.setId("4");
        note4.setLastModifyTime(setTime(2019, 1, 12));
        note4.setTitle("note4");
        List<Attachment> attachments4 = new ArrayList<>();
        note4.setAttachments(attachments4);

        Note note5 = Note.newInstance();
        note5.setId("5");
        note5.setLastModifyTime(setTime(2019, 1, 12));
        note5.setTitle("note5");
        List<Attachment> attachments5 = new ArrayList<>();
        note5.setAttachments(attachments5);

        List<Note> list = new ArrayList<>();
        list.add(note);
        list.add(note2);
        list.add(note3);
        list.add(note4);
        list.add(note5);
        return list;
    }
    public static String getUsername(){
        return "806784568@qq.com";
    }

    public static String getPassword(){
        return "";
    }

    public static DavClient getClient(){
        return new SardineDavClient(getUsername(),getPassword());
    }
    public static ExecutorService getExecutorService(){
        return Executors.newCachedThreadPool();
    }

    @Test
    public void split(){
        String[] split = StringUtils.split("|dafdo|", "|");
        for (String s : split) {
            System.out.println("|"+s+"|");
        }
        String[] split2 = "|dafdo|".split( "\\|");
        for (String s : split2) {
            System.out.println("|"+s+"|");
        }
    }
}
