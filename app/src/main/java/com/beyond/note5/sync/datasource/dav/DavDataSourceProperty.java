package com.beyond.note5.sync.datasource.dav;

import com.beyond.note5.sync.datasource.SyncProperty;

import static com.beyond.note5.MyApplication.DAV_DATA_DIR;
import static com.beyond.note5.MyApplication.DAV_LOCK_DIR;
import static com.beyond.note5.MyApplication.DAV_STAMP_BASE_PREFIX;
import static com.beyond.note5.MyApplication.DAV_STAMP_DIR;
import static com.beyond.note5.MyApplication.DAV_STAMP_LATEST_NAME;

/**
 * @author: beyond
 * @date: 2019/8/9
 */

public class DavDataSourceProperty extends SyncProperty{

    private String username;
    private String password;
    private String server;
    private String lockPath = DAV_LOCK_DIR;
    private String syncStampPath = DAV_STAMP_DIR;
    private String dataPath = DAV_DATA_DIR;
    private boolean needExecutorService = true;
    private String latestSyncStampFileName = DAV_STAMP_LATEST_NAME;
    private String baseSyncStampFilePrefix = DAV_STAMP_BASE_PREFIX;

    public DavDataSourceProperty(String username, String password, String server) {
        this.username = username;
        this.password = password;
        this.server = server;
    }

    public String getLatestSyncStampFileName() {
        return latestSyncStampFileName;
    }

    public void setLatestSyncStampFileName(String latestSyncStampFileName) {
        this.latestSyncStampFileName = latestSyncStampFileName;
    }

    public String getBaseSyncStampFilePrefix() {
        return baseSyncStampFilePrefix;
    }

    public void setBaseSyncStampFilePrefix(String baseSyncStampFilePrefix) {
        this.baseSyncStampFilePrefix = baseSyncStampFilePrefix;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getLockPath() {
        return lockPath;
    }

    public void setLockPath(String lockPath) {
        this.lockPath = lockPath;
    }

    public String getSyncStampPath() {
        return syncStampPath;
    }

    public void setSyncStampPath(String syncStampPath) {
        this.syncStampPath = syncStampPath;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public boolean isNeedExecutorService() {
        return needExecutorService;
    }

    public void setNeedExecutorService(boolean needExecutorService) {
        this.needExecutorService = needExecutorService;
    }

}
