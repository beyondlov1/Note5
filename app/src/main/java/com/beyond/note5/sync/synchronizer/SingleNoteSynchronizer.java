package com.beyond.note5.sync.synchronizer;

import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.DistributedDavDataSource;
import com.beyond.note5.sync.datasource.SingleDavDataSource;
import com.beyond.note5.sync.webdav.DavLock;
import com.beyond.note5.sync.webdav.Lock;
import com.beyond.note5.utils.PreferenceUtil;

import java.util.Date;

public class SingleNoteSynchronizer extends SingleSynchronizerBase<Note> {

    private static final String NOTE_LAST_SYNC_TIME  = "note.last.syncNote.time";

    @Override
    protected void saveLastSyncTime(Long time) {
        PreferenceUtil.put(NOTE_LAST_SYNC_TIME,time);
    }

    @Override
    protected Date getLastSyncTime() {
//        return new Date(2014,12,3);
        return new Date(PreferenceUtil.getLong(NOTE_LAST_SYNC_TIME));
    }

    @Override
    protected Lock getRemoteLock(DataSource<Note> remote) {
        if (remote instanceof SingleDavDataSource){
            return new DavLock(((SingleDavDataSource)remote).getUploadUrl()+".lock");
        }else if (remote instanceof DistributedDavDataSource){
            return new DavLock(((DistributedDavDataSource<Note>) remote).getLockUrl());
        }else {
            throw new RuntimeException("暂不支持");
        }
    }

}
