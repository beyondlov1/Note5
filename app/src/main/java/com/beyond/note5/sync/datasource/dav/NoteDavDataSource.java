package com.beyond.note5.sync.datasource.dav;

import android.util.Log;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.webdav.client.DavClient;
import com.beyond.note5.utils.OkWebDavUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class NoteDavDataSource extends DefaultDavDataSource<Note> {


    public NoteDavDataSource(String oppositeKey, DavClient client, String server, DavPathStrategy pathStrategy, ExecutorService executorService, Class<Note> clazz) {
        super(oppositeKey, client, server, pathStrategy, executorService, clazz);
    }

    @Override
    public void add(Note note) throws IOException {
        super.add(note);

        String server = getServer();
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

}
