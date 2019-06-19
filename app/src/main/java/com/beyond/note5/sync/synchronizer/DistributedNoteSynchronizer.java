package com.beyond.note5.sync.synchronizer;

import com.beyond.note5.bean.Note;
import com.beyond.note5.utils.PreferenceUtil;

import java.util.Date;

public class DistributedNoteSynchronizer extends DistributedSynchronizerBase<Note> {

    private static final String NOTE_LAST_SYNC_TIME  = "note.last.syncNote.time";

    @Override
    protected void saveLastSyncTime(Long time) {
        PreferenceUtil.put(NOTE_LAST_SYNC_TIME,time);
    }

    @Override
    protected Date getLastSyncTime(Note note) {
//        return new Date(2014,12,3);
        return new Date(PreferenceUtil.getLong(NOTE_LAST_SYNC_TIME));
    }

}
