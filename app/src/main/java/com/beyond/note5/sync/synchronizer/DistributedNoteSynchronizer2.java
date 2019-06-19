package com.beyond.note5.sync.synchronizer;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Note;
import com.beyond.note5.bean.SyncInfo;
import com.beyond.note5.model.dao.SyncInfoDao;
import com.beyond.note5.sync.datasource.DavDataSource;
import com.beyond.note5.sync.datasource.DavDataSourceBase;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.PreferenceUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DistributedNoteSynchronizer2 extends DistributedSynchronizerBase<Note> {

    private static final String NOTE_LAST_SYNC_TIME  = "note.last.syncNote.time";

    private SyncInfoDao syncInfoDao;

    public DistributedNoteSynchronizer2() {
        syncInfoDao = MyApplication.getInstance().getDaoSession().getSyncInfoDao();
    }

    @Override
    protected void saveLastSyncTime(Long time) {
        if (remote instanceof DavDataSource){
            List<SyncInfo> updateList= new ArrayList<>();
            List<SyncInfo> insertList= new ArrayList<>();
            String[] nodes = ((DavDataSource) remote).getNodes();
            String[] paths = ((DavDataSource) remote).getPaths();
            List<SyncInfo> syncInfos = syncInfoDao.queryBuilder().list();
            for (String node : nodes) {
                for (String path : paths) {
                    boolean found = false;
                    for (SyncInfo syncInfo : syncInfos) {
                        if (StringUtils.equals(syncInfo.getNode(),node)
                                && StringUtils.equals(syncInfo.getPath(),path)){
                            syncInfo.setLastSyncTime(new Date(time));
                            updateList.add(syncInfo);
                            found = true;
                            break;
                        }
                    }
                    if (!found){
                        SyncInfo syncInfo = new SyncInfo();
                        syncInfo.setId(IDUtil.uuid());
                        syncInfo.setNode(node);
                        syncInfo.setPath(path);
                        syncInfo.setLastSyncTime(new Date(time));
                        insertList.add(syncInfo);
                    }
                }
            }
            syncInfoDao.insertInTx(insertList);
            syncInfoDao.updateInTx(updateList);
        }else {
            PreferenceUtil.put(NOTE_LAST_SYNC_TIME,time);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Date getLastSyncTime(Note note) {
        if (remote instanceof DavDataSourceBase){
            String node = ((DavDataSourceBase) remote).getNode(note);
            String path = ((DavDataSourceBase) remote).getPath(note);
            SyncInfo syncInfo = syncInfoDao.queryBuilder()
                    .where(SyncInfoDao.Properties.Node.eq(node))
                    .where(SyncInfoDao.Properties.Path.eq(path))
                    .unique();
            if (syncInfo!=null){
                return syncInfo.getLastSyncTime();
            }else {
                return new Date(0);
            }
        }else {
            return new Date(PreferenceUtil.getLong(NOTE_LAST_SYNC_TIME));
        }
    }

}
