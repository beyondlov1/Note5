package com.beyond.note5.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Attachment {

    @Id
    private String id;

    private String noteId;

    private String name;
    private String type;
    private String path;

    @Generated(hash = 2055390652)
    public Attachment(String id, String noteId, String name, String type,
            String path) {
        this.id = id;
        this.noteId = noteId;
        this.name = name;
        this.type = type;
        this.path = path;
    }

    @Generated(hash = 1924760169)
    public Attachment() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }
}
