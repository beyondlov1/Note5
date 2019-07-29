package com.beyond.note5.sync.datasource.impl;

import android.util.Log;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.DavDataSource;
import com.beyond.note5.sync.datasource.DavPathStrategy;
import com.beyond.note5.sync.exception.SaveException;
import com.beyond.note5.sync.model.bean.TraceInfo;
import com.beyond.note5.sync.webdav.client.DavClient;
import com.beyond.note5.utils.OkWebDavUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

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
                    upload(
                            getRemoteUrl(note, server, attachment),
                            getLocalPath(attachment)
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
    public List<Note> getChangedData(TraceInfo traceInfo) throws IOException {
        return defaultDavDataSource.getChangedData(traceInfo);
    }

    @Override
    public void save(Note note) throws IOException {
        defaultDavDataSource.save(note);

        String server = defaultDavDataSource.getServer();
        List<Attachment> attachments = note.getAttachments();
        if (attachments!= null && !attachments.isEmpty()){
            for (Attachment attachment : attachments) {
                // 如果上传过就不再上传
                if (getClient().exists(getRemoteUrl(note, server, attachment))){
                    continue;
                }
                if (new File(attachment.getPath()).exists()){
                    upload(
                            getRemoteUrl(note, server, attachment),
                            getLocalPath(attachment)
                    );
                }else {
                    Log.i(getClass().getSimpleName(),"附件不存在");
                }
            }
        }
    }

    @Override
    public void saveAll(List<Note> ts) throws IOException, SaveException {
        if (defaultDavDataSource.executorService == null){
            singleThreadSaveAll(ts);
            return;
        }
        multiThreadSaveAll(ts);
    }

    private void multiThreadSaveAll(List<Note> ts) throws SaveException {
        defaultDavDataSource.mkDirForMultiThread();
        CompletionService<Note> completionService = new ExecutorCompletionService<>(defaultDavDataSource.executorService);
        for (Note t : ts) {
            completionService.submit(new Callable<Note>() {
                @Override
                public Note call() throws Exception {
                    save(t);
                    return t;
                }
            });
        }

        List<Note> result = new ArrayList<>();
        try {
            for (int i = 0; i < ts.size(); i++) {
                Future<Note> future = completionService.take();
                Note t = future.get();
                if (t != null) {
                    result.add(t);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Log.e(getClass().getSimpleName(), "", e);
            List<String> successIds = new ArrayList<>(result.size());
            for (Note t : result) {
                successIds.add(t.getId());
            }
            throw new SaveException(e,getKey(),successIds);
        }
    }

    private void singleThreadSaveAll(List<Note> ts) throws SaveException {
        List<String> successIds = new ArrayList<>();
        for (Note t : ts) {
            try {
                save(t);
                successIds.add(t.getId());
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "save失败", e);
                throw new SaveException(e,getKey(),successIds);
            }
        }
    }

    @Override
    public void saveAll(List<Note> notes, String source) throws IOException, SaveException {
        saveAll(notes);
    }

    @Override
    public boolean isChanged(DataSource<Note> targetDataSource) throws IOException {
        return defaultDavDataSource.isChanged(targetDataSource);
    }

    @Override
    public TraceInfo getCorrespondTraceInfo(DataSource<Note> targetDataSource) throws IOException {
        return defaultDavDataSource.getCorrespondTraceInfo(targetDataSource);
    }

    @Override
    public void setCorrespondTraceInfo(TraceInfo traceInfo, DataSource<Note> targetDataSource) throws IOException {
        defaultDavDataSource.setCorrespondTraceInfo(traceInfo,targetDataSource);
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
    public TraceInfo getLatestTraceInfo() throws IOException {
        return defaultDavDataSource.getLatestTraceInfo();
    }

    @Override
    public void setLatestTraceInfo(TraceInfo traceInfo) throws IOException {
        defaultDavDataSource.setLatestTraceInfo(traceInfo);
    }

    @Override
    public DavPathStrategy getPathStrategy() {
        return defaultDavDataSource.getPathStrategy();
    }

    @Override
    public void upload(String url, String path) throws IOException {
        defaultDavDataSource.upload(url, path);
    }

    @Override
    public void download(String url, String path) throws IOException {
        defaultDavDataSource.download(url, path);
    }
}
