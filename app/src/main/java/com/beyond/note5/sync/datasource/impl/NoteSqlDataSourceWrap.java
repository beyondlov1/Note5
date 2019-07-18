package com.beyond.note5.sync.datasource.impl;

import android.util.Log;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.DavDataSource;
import com.beyond.note5.sync.model.bean.TraceInfo;
import com.beyond.note5.utils.OkWebDavUtil;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class NoteSqlDataSourceWrap implements DataSource<Note> {

    private NoteSqlDataSource noteSqlDataSource;

    private DavDataSource<Note> davDataSource;

    public NoteSqlDataSourceWrap(NoteSqlDataSource noteSqlDataSource, DavDataSource<Note> davDataSource) {
        this.noteSqlDataSource = noteSqlDataSource;
        this.davDataSource = davDataSource;
        setTargetDataSourceKey(davDataSource.getKey());
    }

    @Override
    public String getKey() {
        return noteSqlDataSource.getKey();
    }

    @Override
    public void add(Note note) {
        noteSqlDataSource.add(note);

        List<Attachment> attachments = note.getAttachments();
        if (attachments != null && !attachments.isEmpty()) {
            for (Attachment attachment : attachments) {
                try {
                    if (new File(attachment.getPath()).exists()){
                        continue;
                    }
                    davDataSource.getClient().download(
                            getRemoteUrl(note, attachment),
                            getLocalPath(attachment)
                    );
                } catch (IOException e) {
                    Log.e(this.getClass().getSimpleName(), "下载文件失败", e);
                }
            }
        }
    }

    private String getLocalPath(Attachment attachment) {
        return attachment.getPath();
    }

    private String getRemoteUrl(Note note, Attachment attachment) {
        return OkWebDavUtil.concat(
                OkWebDavUtil.concat(davDataSource.getServer(), davDataSource.getPath(note)),
                getLocalPath(attachment).replaceFirst(MyApplication.getInstance().getFileStorageDir().getAbsolutePath(), "/files")
        );
    }

    @Override
    public void delete(Note note) {
        noteSqlDataSource.delete(note);
    }

    @Override
    public void update(Note note) {
        noteSqlDataSource.update(note);
    }

    @Override
    public Note select(Note note) {
        return noteSqlDataSource.select(note);
    }

    @Override
    public Note selectById(String id) throws IOException {
        return noteSqlDataSource.selectById(id);
    }

    @Override
    public List<Note> selectByIds(List<String> ids) {
        return noteSqlDataSource.selectByIds(ids);
    }

    @Override
    public List<Note> selectAll() throws IOException {
        return noteSqlDataSource.selectAll();
    }

    @Override
    public List<Note> selectByModifiedDate(Date date) throws IOException {
        return noteSqlDataSource.selectByModifiedDate(date);
    }

    @Override
    public TraceInfo getLatestTraceInfo() throws IOException {
        return noteSqlDataSource.getLatestTraceInfo();
    }

    @Override
    public void setLatestTraceInfo(TraceInfo traceInfo) throws IOException {
        noteSqlDataSource.setLatestTraceInfo(traceInfo);
    }

    @Override
    public void cover(List<Note> all) throws IOException {
        noteSqlDataSource.cover(all);
    }

    @Override
    public Class<Note> clazz() {
        return noteSqlDataSource.clazz();
    }

    @Override
    public List<Note> getModifiedData(TraceInfo traceInfo) throws IOException {
        return noteSqlDataSource.getModifiedData(traceInfo);
    }

    @Override
    public void save(Note note) throws IOException {
        noteSqlDataSource.save(note);

        List<Attachment> attachments = note.getAttachments();
        if (attachments != null && !attachments.isEmpty()) {
            for (Attachment attachment : attachments) {
                try {
                    if (new File(attachment.getPath()).exists()){
                        continue;
                    }
                    davDataSource.getClient().download(
                            getRemoteUrl(note, attachment),
                            getLocalPath(attachment)
                    );
                } catch (IOException e) {
                    Log.e(this.getClass().getSimpleName(), "下载文件失败", e);
                }
            }
        }
    }

    @Override
    public void saveAll(List<Note> notes) throws IOException {
       noteSqlDataSource.saveAll(notes);

        for (Note note : notes) {
            List<Attachment> attachments = note.getAttachments();
            if (attachments != null && !attachments.isEmpty()) {
                for (Attachment attachment : attachments) {
                    try {
                        if (new File(attachment.getPath()).exists()){
                            continue;
                        }
                        davDataSource.getClient().download(
                                getRemoteUrl(note, attachment),
                                getLocalPath(attachment)
                        );
                    } catch (IOException e) {
                        Log.e(this.getClass().getSimpleName(), "下载文件失败", e);
                    }
                }
            }
        }
    }

    @Override
    public boolean isChanged(DataSource<Note> targetDataSource) throws IOException {
        return noteSqlDataSource.isChanged(targetDataSource);
    }

    @Override
    public TraceInfo getCorrespondTraceInfo(DataSource<Note> targetDataSource) throws IOException {
        return noteSqlDataSource.getCorrespondTraceInfo(targetDataSource);
    }

    @Override
    public void setCorrespondTraceInfo(TraceInfo traceInfo, DataSource<Note> targetDataSource) throws IOException {
        noteSqlDataSource.setCorrespondTraceInfo(traceInfo, targetDataSource);
    }

    @Override
    public void setTargetDataSourceKey(String targetDataSourceKey) {
        noteSqlDataSource.setTargetDataSourceKey(targetDataSourceKey);
    }

    @Override
    public boolean tryLock() {
        return noteSqlDataSource.tryLock();
    }

    @Override
    public boolean tryLock(Long time) {
        return noteSqlDataSource.tryLock(time);
    }

    @Override
    public boolean isLocked() {
        return noteSqlDataSource.isLocked();
    }

    @Override
    public boolean release() {
        return noteSqlDataSource.release();
    }
}
