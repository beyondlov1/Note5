package com.beyond.note5.sync.datasource;

import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.webdav.BasicAuthenticator;
import com.beyond.note5.utils.IDUtil;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import static com.beyond.note5.utils.StringCompressUtilTest.setTime;

public class DistributedDavDataSourceTest {

    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10000, TimeUnit.MILLISECONDS)
            .readTimeout(10000, TimeUnit.MILLISECONDS)
            .authenticator(new BasicAuthenticator("",
                    ""))
            .build();

    @Test
    public void add() {
        DistributedDavDataSource<Note> distributedDavDataSource = new DistributedDavDataSource<Note>(client,
                getRootUrl()) {
            @Override
            public Class clazz() {
                return Note.class;
            }
        };

        distributedDavDataSource.add(getExampleNotes().get(0));
        distributedDavDataSource.add(getExampleNotes().get(1));
    }

    @Test
    public void distributeAdd(){

        DistributedDavDataSource<Note> distributedDavDataSource = new DistributedDavDataSource<Note>(client,
                getRootUrl(),getRootUrl()+"NoteCloud2") {
            @Override
            public Class clazz() {
                return Note.class;
            }
        };

        distributedDavDataSource.add(getExampleNotes().get(0));
        distributedDavDataSource.add(getExampleNotes().get(1));
        distributedDavDataSource.add(getExampleNotes().get(1));
        distributedDavDataSource.add(getExampleNotes().get(1));
    }

    @Test
    public void delete() {
    }

    @Test
    public void update() {
    }

    @Test
    public void select() {
    }

    @Test
    public void selectAll() {
    }

    @Test
    public void cover() {
    }

    private String getRootUrl(){
        return "https://dav.jianguoyun.com/dav/";
    }

    private List<Note> getExampleNotes(){
        Note note = Note.newInstance();
        note.setId("0");
        note.setLastModifyTime(setTime(2019, 1, 12));
        note.setTitle("note");
        List<Attachment> attachments = new ArrayList<>();
        note.setAttachments(attachments);

        Note note2 = Note.newInstance();
        note2.setId(IDUtil.uuid());
        note2.setLastModifyTime(setTime(2019, 1, 12));
        note2.setTitle("note2");
        List<Attachment> attachments2 = new ArrayList<>();
        note2.setAttachments(attachments2);

        List<Note> list = new ArrayList<>();
        list.add(note);
        list.add(note2);
        return list;
    }
}