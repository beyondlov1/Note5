package com.beyond.note5.bean;

import com.beyond.note5.constant.DocumentConst;
import com.beyond.note5.model.dao.DaoSession;
import com.beyond.note5.model.dao.ReminderDao;
import com.beyond.note5.model.dao.TodoDao;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.Date;

@Entity
public class Todo extends Document {

    @Id
    private String id;
    private String reminderId;
    private String title;
    private String content;
    private String type = Document.TODO;
    private Date createTime;
    private Date lastModifyTime;
    private Integer version;
    private Integer readFlag = DocumentConst.READ_FLAG_NORMAL;

    @ToOne(joinProperty = "reminderId")
    private Reminder reminder;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 1860181255)
    private transient TodoDao myDao;

    @Generated(hash = 1601477535)
    public Todo(String id, String reminderId, String title, String content,
            String type, Date createTime, Date lastModifyTime, Integer version,
            Integer readFlag) {
        this.id = id;
        this.reminderId = reminderId;
        this.title = title;
        this.content = content;
        this.type = type;
        this.createTime = createTime;
        this.lastModifyTime = lastModifyTime;
        this.version = version;
        this.readFlag = readFlag;
    }

    @Generated(hash = 1698043777)
    public Todo() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReminderId() {
        return this.reminderId;
    }

    public void setReminderId(String reminderId) {
        this.reminderId = reminderId;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastModifyTime() {
        return this.lastModifyTime;
    }

    public void setLastModifyTime(Date lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getReadFlag() {
        return this.readFlag;
    }

    public void setReadFlag(Integer readFlag) {
        this.readFlag = readFlag;
    }

    @Generated(hash = 1824336716)
    private transient String reminder__resolvedKey;

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 496639938)
    public Reminder getReminder() {
        String __key = this.reminderId;
        if (reminder__resolvedKey == null || reminder__resolvedKey != __key) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ReminderDao targetDao = daoSession.getReminderDao();
            Reminder reminderNew = targetDao.load(__key);
            synchronized (this) {
                reminder = reminderNew;
                reminder__resolvedKey = __key;
            }
        }
        return reminder;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 668954891)
    public void setReminder(Reminder reminder) {
        synchronized (this) {
            this.reminder = reminder;
            reminderId = reminder == null ? null : reminder.getId();
            reminder__resolvedKey = reminderId;
        }
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
    @Generated(hash = 1209008365)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getTodoDao() : null;
    }
}
