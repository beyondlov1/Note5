package com.beyond.note5.bean;

import com.beyond.note5.constant.DocumentConst;
import com.beyond.note5.model.dao.DaoSession;
import com.beyond.note5.model.dao.ReminderDao;
import com.beyond.note5.model.dao.TodoDao;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.TimeNLPUtil;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.Date;

@SuppressWarnings("StringEquality")
@Entity
public class Todo extends Document {

    public static Todo create(String content){
        Todo todo = new Todo();
        todo.setId(IDUtil.uuid());
        todo.setTitle(content.length() > 10 ? content.substring(0, 10) : content);
        todo.setContent(content);
        Date currDate = new Date();
        todo.setCreateTime(currDate);
        todo.setVersion(0);
        todo.setLastModifyTime(currDate);
        todo.setReadFlag(DocumentConst.READ_FLAG_NORMAL);
        todo.setValid(true);

        Date reminderStart = TimeNLPUtil.parse(todo.getContent());
        if (reminderStart!=null){
            Reminder reminder = new Reminder();
            reminder.setId(IDUtil.uuid());
            reminder.setStart(reminderStart);
            todo.setReminder(reminder);
            todo.setReminderId(reminder.getId());
        }
        return todo;
    }

    @Id
    private String id;
    private String reminderId;
    private String title;
    private String content;
    private String contentWithoutTime;
    private String type = Document.TODO;
    private Date createTime;
    private Date lastModifyTime;
    private Integer version;
    private Integer readFlag = DocumentConst.READ_FLAG_NORMAL;
    private Integer priority = DocumentConst.PRIORITY_DEFAULT;
    private Boolean valid = true;


    @ToOne(joinProperty = "reminderId")
    private Reminder reminder;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 1860181255)
    private transient TodoDao myDao;

    @Generated(hash = 1698043777)
    public Todo() {
    }

    @Generated(hash = 466496526)
    public Todo(String id, String reminderId, String title, String content,
            String contentWithoutTime, String type, Date createTime, Date lastModifyTime,
            Integer version, Integer readFlag, Integer priority, Boolean valid) {
        this.id = id;
        this.reminderId = reminderId;
        this.title = title;
        this.content = content;
        this.contentWithoutTime = contentWithoutTime;
        this.type = type;
        this.createTime = createTime;
        this.lastModifyTime = lastModifyTime;
        this.version = version;
        this.readFlag = readFlag;
        this.priority = priority;
        this.valid = valid;
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

    public String getContentWithoutTime() {
        return this.contentWithoutTime;
    }

    public void setContentWithoutTime(String contentWithoutTime) {
        this.contentWithoutTime = contentWithoutTime;
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
    @Keep
    public Reminder getReminder() {
        String __key = this.reminderId;
        if (reminderId != null && reminder!=null){
            return reminder;
        }
        if (reminderId == null){
            return null;
        }
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

    public Integer getPriority() {
        return this.priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Todo todo = (Todo) super.clone();
        todo.setReminderId(reminderId);
        todo.setReminder(reminder);
        todo.setContentWithoutTime(contentWithoutTime);
        todo.setType(type);
        return todo;
    }

    @Override
    public Boolean getValid() {
        return valid;
    }

    @Override
    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Todo todo = (Todo) o;

        if (id != null ? !id.equals(todo.id) : todo.id != null) return false;
        if (reminderId != null ? !reminderId.equals(todo.reminderId) : todo.reminderId != null)
            return false;
        if (title != null ? !title.equals(todo.title) : todo.title != null) return false;
        if (content != null ? !content.equals(todo.content) : todo.content != null) return false;
        if (contentWithoutTime != null ? !contentWithoutTime.equals(todo.contentWithoutTime) : todo.contentWithoutTime != null)
            return false;
        if (type != null ? !type.equals(todo.type) : todo.type != null) return false;
        if (createTime != null ? !createTime.equals(todo.createTime) : todo.createTime != null)
            return false;
        if (lastModifyTime != null ? !lastModifyTime.equals(todo.lastModifyTime) : todo.lastModifyTime != null)
            return false;
        if (version != null ? !version.equals(todo.version) : todo.version != null) return false;
        if (readFlag != null ? !readFlag.equals(todo.readFlag) : todo.readFlag != null)
            return false;
        if (priority != null ? !priority.equals(todo.priority) : todo.priority != null)
            return false;
        if (valid != null ? !valid.equals(todo.valid) : todo.valid != null) return false;
        return reminder != null ? reminder.equals(todo.reminder) : todo.reminder == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (reminderId != null ? reminderId.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (contentWithoutTime != null ? contentWithoutTime.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (lastModifyTime != null ? lastModifyTime.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (readFlag != null ? readFlag.hashCode() : 0);
        result = 31 * result + (priority != null ? priority.hashCode() : 0);
        result = 31 * result + (valid != null ? valid.hashCode() : 0);
        result = 31 * result + (reminder != null ? reminder.hashCode() : 0);
        return result;
    }
}
