package com.beyond.note5.sync.datasource.dav;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Document;
import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.DataSource;
import com.beyond.note5.utils.OkWebDavUtil;

import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public abstract class DavDataSource<T extends Document> implements DataSource<T> {

    protected String url;

    public DavDataSource(String url) {
        this.url = url;
    }

    @Override
    public void add(T document) {
        throw new RuntimeException("暂不支持");
    }

    @Override
    public void delete(T document) {
        throw new RuntimeException("暂不支持");
    }

    @Override
    public void update(T document) {
        throw new RuntimeException("暂不支持");
    }

    @Override
    public T select(T document) {
        throw new RuntimeException("暂不支持");
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> selectAll() throws IOException {
        String data = download(getDownloadUrl());
        return JSONObject.parseArray(data,clazz());
    }

    @Override
    public void cover(List<T> all) throws IOException {
        String jsonString = JSONObject.toJSONString(all);
        upload(getUploadUrl(), jsonString);
    }

    @Override
    public abstract Class clazz();

    private void upload(String url, String content) {

        final Request request = new Request.Builder()
                .url(url)
                .method("PUT",RequestBody.create(MediaType.get("application/x-www-form-urlencoded"),content.getBytes()))
                .build();
        try ( Response mkResponse = OkWebDavUtil.mkRemoteDir(url);Response response = OkWebDavUtil.requestForResponse(request)) {
            if (!mkResponse.isSuccessful()){
                throw new RuntimeException("创建文件夹失败");
            }

            if (!response.isSuccessful()) {
                throw new RuntimeException("上传失败");
            }
        } catch (Exception e) {
            Log.e("dav", "request fail");
            throw new RuntimeException("上传失败");
        }

    }

    private String download(String url) throws IOException {
        final Request request = new Request.Builder()
                .url(url)
                .method("GET",null)
                .build();

        return OkWebDavUtil.requestForString(request, new OkWebDavUtil.Callback<String, String>() {
            @Override
            public String onSuccess(String s) {
                return s;
            }

            @Override
            public void onFail() {
                upload(url, "");
            }
        });
    }

    public String getDownloadUrl() {
        return url;
    }

    public String getUploadUrl() {
        return url;
    }


    public static void main(String[] args) throws IOException {
        DavDataSource<Note> davDataSource = new NoteDavDataSource("https://dav.jianguoyun.com/dav/NoteClould2/test");

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

        davDataSource.cover(remoteList);
        List<Note> list = davDataSource.selectAll();
        System.out.println(list);
    }

    private static Date setTime(int year, int month, int day) {
        Calendar instance = Calendar.getInstance();
        instance.set(year, month, day);
        return instance.getTime();
    }
}
