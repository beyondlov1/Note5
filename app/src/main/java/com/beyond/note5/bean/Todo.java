package com.beyond.note5.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;

@Entity
public class Todo extends Document {

    @Id
    private String id;
    private String title;
    private String content;
    private String type;
    private Date createTime;
    private Date lastModifyTime;
    private Integer version;
    private Integer readFlag;

    @Generated(hash = 1800421537)
    public Todo(String id, String title, String content, String type,
                Date createTime, Date lastModifyTime, Integer version,
                Integer readFlag) {
        this.id = id;
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
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return Document.TODO;
    }

    @Override
    public Integer getReadFlag() {
        return readFlag;
    }

    @Override
    public void setReadFlag(Integer readFlag) {
        this.readFlag = readFlag;
    }
}
