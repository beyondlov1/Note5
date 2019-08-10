package com.beyond.note5.bean;

import com.beyond.note5.constant.DocumentConst;
import com.beyond.note5.model.dao.AttachmentDao;
import com.beyond.note5.model.dao.DaoSession;
import com.beyond.note5.model.dao.NoteDao;
import com.beyond.note5.utils.IDUtil;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.Date;
import java.util.List;

@Entity
public class Note extends Document {

    public static Note create(){
        Date currDate = new Date();
        Note note = new Note();
        note.setId(IDUtil.uuid());
        note.setCreateTime(currDate);
        note.setVersion(0);
        note.setLastModifyTime(currDate);
        note.setReadFlag(DocumentConst.READ_FLAG_NORMAL);
        return note;
    }

    @Id
    private String id;
    private String title;
    private String content;
    private String type = Document.NOTE;
    private Date createTime;
    private Date lastModifyTime;
    private Integer version;
    private Integer readFlag = DocumentConst.READ_FLAG_NORMAL;
    private Integer priority = DocumentConst.PRIORITY_DEFAULT;
    private Boolean valid = true;

    @ToMany(referencedJoinProperty = "noteId")
    private List<Attachment> attachments;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 363862535)
    private transient NoteDao myDao;

    public Note() {
    }

    @Generated(hash = 1066351799)
    public Note(String id, String title, String content, String type, Date createTime,
            Date lastModifyTime, Integer version, Integer readFlag, Integer priority, Boolean valid) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.type = type;
        this.createTime = createTime;
        this.lastModifyTime = lastModifyTime;
        this.version = version;
        this.readFlag = readFlag;
        this.priority = priority;
        this.valid = valid;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public Date getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public Date getLastModifyTime() {
        return lastModifyTime;
    }

    @Override
    public void setLastModifyTime(Date lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    @Override
    public Integer getVersion() {
        return version;
    }

    @Override
    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Integer getReadFlag() {
        return readFlag;
    }

    @Override
    public void setReadFlag(Integer readFlag) {
        this.readFlag = readFlag;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }


    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 799086675)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getNoteDao() : null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 237313401)
    public List<Attachment> getAttachments() {
        if (attachments == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            AttachmentDao targetDao = daoSession.getAttachmentDao();
            List<Attachment> attachmentsNew = targetDao._queryNote_Attachments(id);
            synchronized (this) {
                if (attachments == null) {
                    attachments = attachmentsNew;
                }
            }
        }
        return attachments;
    }

    public Integer getPriority() {
        return this.priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        Note note = (Note) super.clone();
        note.setAttachments(this.attachments);
        note.setType(this.type);
        return note;
    }

    @Override
    public String toString() {
        return "Note{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", type='" + type + '\'' +
                ", createTime=" + createTime +
                ", lastModifyTime=" + lastModifyTime +
                ", version=" + version +
                ", readFlag=" + readFlag +
                ", priority=" + priority +
                ", attachments=" + attachments +
                ", daoSession=" + daoSession +
                ", myDao=" + myDao +
                '}';
    }

    @Override
    public Boolean getValid() {
        return valid;
    }

    @Override
    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1946849745)
    public synchronized void resetAttachments() {
        attachments = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Note note = (Note) o;

        if (id != null ? !id.equals(note.id) : note.id != null) return false;
        if (title != null ? !title.equals(note.title) : note.title != null) return false;
        if (content != null ? !content.equals(note.content) : note.content != null) return false;
        if (type != null ? !type.equals(note.type) : note.type != null) return false;
        if (createTime != null ? !createTime.equals(note.createTime) : note.createTime != null)
            return false;
        if (lastModifyTime != null ? !lastModifyTime.equals(note.lastModifyTime) : note.lastModifyTime != null)
            return false;
        if (version != null ? !version.equals(note.version) : note.version != null) return false;
        if (readFlag != null ? !readFlag.equals(note.readFlag) : note.readFlag != null)
            return false;
        if (priority != null ? !priority.equals(note.priority) : note.priority != null)
            return false;
        if (valid != null ? !valid.equals(note.valid) : note.valid != null) return false;
        return attachments != null ? attachments.equals(note.attachments) : note.attachments == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (lastModifyTime != null ? lastModifyTime.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (readFlag != null ? readFlag.hashCode() : 0);
        result = 31 * result + (priority != null ? priority.hashCode() : 0);
        result = 31 * result + (valid != null ? valid.hashCode() : 0);
        result = 31 * result + (attachments != null ? attachments.hashCode() : 0);
        return result;
    }
}
