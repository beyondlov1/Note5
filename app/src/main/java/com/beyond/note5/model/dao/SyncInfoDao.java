package com.beyond.note5.model.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.beyond.note5.sync.model.bean.SyncInfo;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "SYNC_INFO".
*/
public class SyncInfoDao extends AbstractDao<SyncInfo, String> {

    public static final String TABLENAME = "SYNC_INFO";

    /**
     * Properties of entity SyncInfo.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, String.class, "id", true, "ID");
        public final static Property LocalKey = new Property(1, String.class, "localKey", false, "LOCAL_KEY");
        public final static Property RemoteKey = new Property(2, String.class, "remoteKey", false, "REMOTE_KEY");
        public final static Property LastModifyTime = new Property(3, java.util.Date.class, "lastModifyTime", false, "LAST_MODIFY_TIME");
        public final static Property LastSyncTimeStart = new Property(4, java.util.Date.class, "lastSyncTimeStart", false, "LAST_SYNC_TIME_START");
        public final static Property LastSyncTime = new Property(5, java.util.Date.class, "lastSyncTime", false, "LAST_SYNC_TIME");
        public final static Property Type = new Property(6, String.class, "type", false, "TYPE");
    }


    public SyncInfoDao(DaoConfig config) {
        super(config);
    }
    
    public SyncInfoDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"SYNC_INFO\" (" + //
                "\"ID\" TEXT PRIMARY KEY NOT NULL ," + // 0: id
                "\"LOCAL_KEY\" TEXT," + // 1: localKey
                "\"REMOTE_KEY\" TEXT," + // 2: remoteKey
                "\"LAST_MODIFY_TIME\" INTEGER," + // 3: lastModifyTime
                "\"LAST_SYNC_TIME_START\" INTEGER," + // 4: lastSyncTimeStart
                "\"LAST_SYNC_TIME\" INTEGER," + // 5: lastSyncTime
                "\"TYPE\" TEXT);"); // 6: type
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"SYNC_INFO\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, SyncInfo entity) {
        stmt.clearBindings();
 
        String id = entity.getId();
        if (id != null) {
            stmt.bindString(1, id);
        }
 
        String localKey = entity.getLocalKey();
        if (localKey != null) {
            stmt.bindString(2, localKey);
        }
 
        String remoteKey = entity.getRemoteKey();
        if (remoteKey != null) {
            stmt.bindString(3, remoteKey);
        }
 
        java.util.Date lastModifyTime = entity.getLastModifyTime();
        if (lastModifyTime != null) {
            stmt.bindLong(4, lastModifyTime.getTime());
        }
 
        java.util.Date lastSyncTimeStart = entity.getLastSyncTimeStart();
        if (lastSyncTimeStart != null) {
            stmt.bindLong(5, lastSyncTimeStart.getTime());
        }
 
        java.util.Date lastSyncTime = entity.getLastSyncTime();
        if (lastSyncTime != null) {
            stmt.bindLong(6, lastSyncTime.getTime());
        }
 
        String type = entity.getType();
        if (type != null) {
            stmt.bindString(7, type);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, SyncInfo entity) {
        stmt.clearBindings();
 
        String id = entity.getId();
        if (id != null) {
            stmt.bindString(1, id);
        }
 
        String localKey = entity.getLocalKey();
        if (localKey != null) {
            stmt.bindString(2, localKey);
        }
 
        String remoteKey = entity.getRemoteKey();
        if (remoteKey != null) {
            stmt.bindString(3, remoteKey);
        }
 
        java.util.Date lastModifyTime = entity.getLastModifyTime();
        if (lastModifyTime != null) {
            stmt.bindLong(4, lastModifyTime.getTime());
        }
 
        java.util.Date lastSyncTimeStart = entity.getLastSyncTimeStart();
        if (lastSyncTimeStart != null) {
            stmt.bindLong(5, lastSyncTimeStart.getTime());
        }
 
        java.util.Date lastSyncTime = entity.getLastSyncTime();
        if (lastSyncTime != null) {
            stmt.bindLong(6, lastSyncTime.getTime());
        }
 
        String type = entity.getType();
        if (type != null) {
            stmt.bindString(7, type);
        }
    }

    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }    

    @Override
    public SyncInfo readEntity(Cursor cursor, int offset) {
        SyncInfo entity = new SyncInfo( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // localKey
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // remoteKey
            cursor.isNull(offset + 3) ? null : new java.util.Date(cursor.getLong(offset + 3)), // lastModifyTime
            cursor.isNull(offset + 4) ? null : new java.util.Date(cursor.getLong(offset + 4)), // lastSyncTimeStart
            cursor.isNull(offset + 5) ? null : new java.util.Date(cursor.getLong(offset + 5)), // lastSyncTime
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6) // type
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, SyncInfo entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setLocalKey(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setRemoteKey(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setLastModifyTime(cursor.isNull(offset + 3) ? null : new java.util.Date(cursor.getLong(offset + 3)));
        entity.setLastSyncTimeStart(cursor.isNull(offset + 4) ? null : new java.util.Date(cursor.getLong(offset + 4)));
        entity.setLastSyncTime(cursor.isNull(offset + 5) ? null : new java.util.Date(cursor.getLong(offset + 5)));
        entity.setType(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
     }
    
    @Override
    protected final String updateKeyAfterInsert(SyncInfo entity, long rowId) {
        return entity.getId();
    }
    
    @Override
    public String getKey(SyncInfo entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(SyncInfo entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
