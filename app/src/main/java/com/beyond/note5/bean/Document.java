package com.beyond.note5.bean;

import android.util.Log;

import com.beyond.note5.constant.DocumentConst;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;

@Entity
public class Document implements Cloneable,Tracable{

    public final static String NOTE = "note";
    public final static String TODO = "todo";

    @Id
    private String id;
    private String title;
    private String content;
    private String type;
    private Date createTime;
    private Date lastModifyTime;
    private Integer version;
    private Integer readFlag;
    private Integer priority = DocumentConst.PRIORITY_DEFAULT;

    public Document() {
    }

    public Document(String id, String content) {
        this.id = id;
        this.content = content;
    }

    @Generated(hash = 287362452)
    public Document(String id, String title, String content, String type,
            Date createTime, Date lastModifyTime, Integer version, Integer readFlag,
            Integer priority) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.type = type;
        this.createTime = createTime;
        this.lastModifyTime = lastModifyTime;
        this.version = version;
        this.readFlag = readFlag;
        this.priority = priority;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(Date lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Document document = (Document) super.clone();
        try {
            document = this.getClass().newInstance();
            document.setId(this.getId());
            document.setTitle(this.getTitle());
            document.setContent(this.getContent());
            document.setCreateTime(this.getCreateTime());
            document.setLastModifyTime(this.getLastModifyTime());
            document.setVersion(this.getVersion());
            document.setType(this.getType());
            document.setPriority(this.getPriority());
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            Log.e("Document","克隆失败");
        }
        return document;
    }

    public Integer getReadFlag() {
        return readFlag;
    }

    public void setReadFlag(Integer readFlag) {
        this.readFlag = readFlag;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
