package com.beyond.note5.model.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.beyond.note5.bean.Document;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "DOCUMENT".
*/
public class DocumentDao extends AbstractDao<Document, String> {

    public static final String TABLENAME = "DOCUMENT";

    /**
     * Properties of entity Document.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, String.class, "id", true, "ID");
        public final static Property Title = new Property(1, String.class, "title", false, "TITLE");
        public final static Property Content = new Property(2, String.class, "content", false, "CONTENT");
        public final static Property Type = new Property(3, String.class, "type", false, "TYPE");
        public final static Property CreateTime = new Property(4, java.util.Date.class, "createTime", false, "CREATE_TIME");
        public final static Property LastModifyTime = new Property(5, java.util.Date.class, "lastModifyTime", false, "LAST_MODIFY_TIME");
        public final static Property Version = new Property(6, Integer.class, "version", false, "VERSION");
    }


    public DocumentDao(DaoConfig config) {
        super(config);
    }
    
    public DocumentDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DOCUMENT\" (" + //
                "\"ID\" TEXT PRIMARY KEY NOT NULL ," + // 0: id
                "\"TITLE\" TEXT," + // 1: title
                "\"CONTENT\" TEXT," + // 2: content
                "\"TYPE\" TEXT," + // 3: type
                "\"CREATE_TIME\" INTEGER," + // 4: createTime
                "\"LAST_MODIFY_TIME\" INTEGER," + // 5: lastModifyTime
                "\"VERSION\" INTEGER);"); // 6: version
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DOCUMENT\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Document entity) {
        stmt.clearBindings();
 
        String id = entity.getId();
        if (id != null) {
            stmt.bindString(1, id);
        }
 
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(2, title);
        }
 
        String content = entity.getContent();
        if (content != null) {
            stmt.bindString(3, content);
        }
 
        String type = entity.getType();
        if (type != null) {
            stmt.bindString(4, type);
        }
 
        java.util.Date createTime = entity.getCreateTime();
        if (createTime != null) {
            stmt.bindLong(5, createTime.getTime());
        }
 
        java.util.Date lastModifyTime = entity.getLastModifyTime();
        if (lastModifyTime != null) {
            stmt.bindLong(6, lastModifyTime.getTime());
        }
 
        Integer version = entity.getVersion();
        if (version != null) {
            stmt.bindLong(7, version);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Document entity) {
        stmt.clearBindings();
 
        String id = entity.getId();
        if (id != null) {
            stmt.bindString(1, id);
        }
 
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(2, title);
        }
 
        String content = entity.getContent();
        if (content != null) {
            stmt.bindString(3, content);
        }
 
        String type = entity.getType();
        if (type != null) {
            stmt.bindString(4, type);
        }
 
        java.util.Date createTime = entity.getCreateTime();
        if (createTime != null) {
            stmt.bindLong(5, createTime.getTime());
        }
 
        java.util.Date lastModifyTime = entity.getLastModifyTime();
        if (lastModifyTime != null) {
            stmt.bindLong(6, lastModifyTime.getTime());
        }
 
        Integer version = entity.getVersion();
        if (version != null) {
            stmt.bindLong(7, version);
        }
    }

    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }    

    @Override
    public Document readEntity(Cursor cursor, int offset) {
        Document entity = new Document( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // title
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // content
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // type
            cursor.isNull(offset + 4) ? null : new java.util.Date(cursor.getLong(offset + 4)), // createTime
            cursor.isNull(offset + 5) ? null : new java.util.Date(cursor.getLong(offset + 5)), // lastModifyTime
            cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6) // version
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Document entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setTitle(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setContent(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setType(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setCreateTime(cursor.isNull(offset + 4) ? null : new java.util.Date(cursor.getLong(offset + 4)));
        entity.setLastModifyTime(cursor.isNull(offset + 5) ? null : new java.util.Date(cursor.getLong(offset + 5)));
        entity.setVersion(cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6));
     }
    
    @Override
    protected final String updateKeyAfterInsert(Document entity, long rowId) {
        return entity.getId();
    }
    
    @Override
    public String getKey(Document entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Document entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
