package com.beyond.note5.sync.datasource.dav;

import android.util.Log;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Note;
import com.beyond.note5.utils.OkWebDavUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class NotePointDavDataSource extends DefaultPointDavDataSource<Note> {


    public NotePointDavDataSource(DavDataSourceProperty property, Class<Note> clazz) {
        super(property, clazz);
    }

    @Override
    public void add(Note note) throws IOException {
        super.add(note);

        List<Attachment> attachments = note.getAttachments();
        if (attachments!= null && !attachments.isEmpty()){
            for (Attachment attachment : attachments) {
                if (new File(attachment.getPath()).exists()){
                    upload(
                            getRemotePath(note, attachment),
                            getLocalPath(attachment)
                    );
                }else {
                    Log.i(getClass().getSimpleName(),"附件不存在");
                }
            }
        }
    }

    private String getRemotePath(Note note, Attachment attachment) {
        return getRemotePath(note, getLocalPath(attachment));
    }

    private String getRemotePath(Note note, String localPath) {
        return OkWebDavUtil.concat(
                getPath(note),
                localPath.replaceFirst(MyApplication.getInstance().getFileStorageDir().getAbsolutePath(),"/"+property.getFilesDir())
        );
    }

    private String getLocalPath(Attachment attachment) {
        return attachment.getPath();
    }

    @Override
    public void upload(String id, String localPath) throws IOException {
        Note note = new Note();
        note.setId(id);
        getClient().upload(localPath, OkWebDavUtil.concat(getServer(),getRemotePath(note,localPath)));
    }

    @Override
    public void download(String id, String localPath) throws IOException {
        Note note = new Note();
        note.setId(id);
        getClient().download(OkWebDavUtil.concat(getServer(),getPath(note)), localPath);
    }

}
