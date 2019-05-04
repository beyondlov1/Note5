package com.beyond.note5.model.dao;

import java.util.List;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

import com.beyond.note5.bean.Attachment;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "ATTACHMENT".
*/
public class AttachmentDao extends AbstractDao<Attachment, String> {

    public static final String TABLENAME = "ATTACHMENT";

    /**
     * Properties of entity Attachment.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, String.class, "id", true, "ID");
        public final static Property NoteId = new Property(1, String.class, "noteId", false, "NOTE_ID");
        public final static Property Name = new Property(2, String.class, "name", false, "NAME");
        public final static Property Type = new Property(3, String.class, "type", false, "TYPE");
        public final static Property Path = new Property(4, String.class, "path", false, "PATH");
    }

    private Query<Attachment> note_AttachmentsQuery;

    public AttachmentDao(DaoConfig config) {
        super(config);
    }
    
    public AttachmentDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"ATTACHMENT\" (" + //
                "\"ID\" TEXT PRIMARY KEY NOT NULL ," + // 0: id
                "\"NOTE_ID\" TEXT," + // 1: noteId
                "\"NAME\" TEXT," + // 2: name
                "\"TYPE\" TEXT," + // 3: type
                "\"PATH\" TEXT);"); // 4: path
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"ATTACHMENT\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Attachment entity) {
        stmt.clearBindings();
 
        String id = entity.getId();
        if (id != null) {
            stmt.bindString(1, id);
        }
 
        String noteId = entity.getNoteId();
        if (noteId != null) {
            stmt.bindString(2, noteId);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(3, name);
        }
 
        String type = entity.getType();
        if (type != null) {
            stmt.bindString(4, type);
        }
 
        String path = entity.getPath();
        if (path != null) {
            stmt.bindString(5, path);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Attachment entity) {
        stmt.clearBindings();
 
        String id = entity.getId();
        if (id != null) {
            stmt.bindString(1, id);
        }
 
        String noteId = entity.getNoteId();
        if (noteId != null) {
            stmt.bindString(2, noteId);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(3, name);
        }
 
        String type = entity.getType();
        if (type != null) {
            stmt.bindString(4, type);
        }
 
        String path = entity.getPath();
        if (path != null) {
            stmt.bindString(5, path);
        }
    }

    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }    

    @Override
    public Attachment readEntity(Cursor cursor, int offset) {
        Attachment entity = new Attachment( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // noteId
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // name
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // type
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4) // path
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Attachment entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setNoteId(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setName(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setType(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setPath(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
     }
    
    @Override
    protected final String updateKeyAfterInsert(Attachment entity, long rowId) {
        return entity.getId();
    }
    
    @Override
    public String getKey(Attachment entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Attachment entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "attachments" to-many relationship of Note. */
    public List<Attachment> _queryNote_Attachments(String noteId) {
        synchronized (this) {
            if (note_AttachmentsQuery == null) {
                QueryBuilder<Attachment> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.NoteId.eq(null));
                note_AttachmentsQuery = queryBuilder.build();
            }
        }
        Query<Attachment> query = note_AttachmentsQuery.forCurrentThread();
        query.setParameter(0, noteId);
        return query.list();
    }

}
