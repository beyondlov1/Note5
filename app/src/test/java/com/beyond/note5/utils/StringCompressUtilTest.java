package com.beyond.note5.utils;

import com.alibaba.fastjson.JSONObject;
import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Note;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class StringCompressUtilTest {

    @Test
    public void compress() {
        String large = "adfadf的哦啊分配的哦看过哦iegfjai个大家开发骄傲的哦附近啊的皮肤的看vm哦卡的佛大盘附近哦抗哦看v啊的附近哦a" +
                "fdapgodkjgoidg啊公婆看到反对安排疯狂啊的看佛的开发出，xmcaovkdnofiagdknaodfjk";

        large = getExampleStr();
        String compress = StringCompressUtil.compress(large);
        System.out.println(compress);
        System.out.println(compress.length());
        System.out.println(StringCompressUtil.unCompress(compress));
        System.out.println(StringCompressUtil.unCompress(compress).length());
    }

    @Test
    public void unCompress() {
    }

    public static String getExampleStr(){

        Note note = Note.create();
        Note note1 = Note.create();
        Note note2 = Note.create();
        Note note3 = Note.create();
        Note note4 = Note.create();
        Note note5 = Note.create();
        Note note6 = Note.create();

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

        localList.addAll(remoteList);
        localList.addAll(remoteList);
        localList.addAll(remoteList);
        localList.addAll(remoteList);
        localList.addAll(remoteList);
        localList.addAll(remoteList);
        localList.addAll(remoteList);
        localList.addAll(remoteList);
        localList.addAll(remoteList);
        localList.addAll(remoteList);
        localList.addAll(remoteList);
        localList.addAll(remoteList);

        return JSONObject.toJSONString(localList);
    }

    public static Date setTime(int year, int month, int day) {
        Calendar instance = Calendar.getInstance();
        instance.set(year, month, day);
        return instance.getTime();
    }
}