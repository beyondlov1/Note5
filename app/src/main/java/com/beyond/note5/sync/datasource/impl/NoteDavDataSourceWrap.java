package com.beyond.note5.sync.datasource.impl;

import android.util.Log;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.DavDataSource;
import com.beyond.note5.sync.datasource.DavPathStrategy;
import com.beyond.note5.sync.model.bean.TraceInfo;
import com.beyond.note5.sync.webdav.client.DavClient;
import com.beyond.note5.utils.OkWebDavUtil;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class NoteDavDataSourceWrap implements DavDataSource<Note> {

    private DefaultDavDataSource<Note> defaultDavDataSource;


    public NoteDavDataSourceWrap(DefaultDavDataSource<Note> defaultDavDataSource) {
        this.defaultDavDataSource = defaultDavDataSource;
    }

    @Override
    public String getKey() {
        return defaultDavDataSource.getKey();
    }

    @Override
    public void add(Note note) throws IOException {
        defaultDavDataSource.add(note);
        String server = defaultDavDataSource.getServer();
        List<Attachment> attachments = note.getAttachments();
        if (attachments!= null && !attachments.isEmpty()){
            for (Attachment attachment : attachments) {
                if (new File(attachment.getPath()).exists()){
                    getClient().upload(
                            getLocalPath(attachment),
                            getRemoteUrl(note, server, attachment)
                    );
                }else {
                    Log.i(getClass().getSimpleName(),"附件不存在");
                }
            }
        }
    }

    private String getRemoteUrl(Note note, String server, Attachment attachment) {
        return OkWebDavUtil.concat(
                OkWebDavUtil.concat(server,getPath(note)),
                getLocalPath(attachment).replaceFirst(MyApplication.getInstance().getFileStorageDir().getAbsolutePath(),"/files")
        );
    }

    private String getLocalPath(Attachment attachment) {
        return attachment.getPath();
    }

    @Override
    public void delete(Note note) throws IOException {
        defaultDavDataSource.delete(note);
    }

    @Override
    public void update(Note note) throws IOException {
        defaultDavDataSource.update(note);
    }

    @Override
    public Note select(Note note) throws IOException {
        return defaultDavDataSource.select(note);
    }

    @Override
    public Note selectById(String id) throws IOException {
        return defaultDavDataSource.selectById(id);
    }

    @Override
    public List<Note> selectByIds(List<String> ids) throws IOException {
        return defaultDavDataSource.selectByIds(ids);
    }

    public List<Note> selectAllValid() throws IOException {
        return defaultDavDataSource.selectAllValid();
    }

    @Override
    public List<Note> selectAll() throws IOException {
        return defaultDavDataSource.selectAll();
    }

    @Override
    public void cover(List<Note> all) {
        defaultDavDataSource.cover(all);
    }

    @Override
    public Class<Note> clazz() {
        return defaultDavDataSource.clazz();
    }

    @Override
    public boolean tryLock(Long time) {
        return defaultDavDataSource.tryLock(time);
    }

    @Override
    public boolean isLocked() {
        return defaultDavDataSource.isLocked();
    }

    @Override
    public boolean tryLock() {
        return defaultDavDataSource.tryLock();
    }

    @Override
    public boolean release() {
        return defaultDavDataSource.release();
    }

    @Override
    public String getServer() {
        return defaultDavDataSource.getServer();
    }

    @Override
    public String[] getPaths() {
        return defaultDavDataSource.getPaths();
    }

    @Override
    public String getPath(Note note) {
        return defaultDavDataSource.getPath(note);
    }

    @Override
    public DavClient getClient() {
        return defaultDavDataSource.getClient();
    }

    @Override
    public TraceInfo getTraceInfo(DataSource<Note> targetDataSource) throws IOException {
        return defaultDavDataSource.getTraceInfo(targetDataSource);
    }

    @Override
    public void setTraceInfo(TraceInfo traceInfo, DataSource<Note> targetDataSource) throws IOException {
        defaultDavDataSource.setTraceInfo(traceInfo,targetDataSource);
    }

    @Override
    public DavPathStrategy getPathStrategy() {
        return defaultDavDataSource.getPathStrategy();
    }

    @Override
    public List<Note> selectByModifiedDate(Date date) throws IOException {
        return defaultDavDataSource.selectByModifiedDate(date);
    }


}
