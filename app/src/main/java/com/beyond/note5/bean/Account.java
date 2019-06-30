package com.beyond.note5.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Account {
    @Id
    private String id;
    private String server;
    private String username;
    private String password;
    private Integer priority = 5;
    private Boolean valid = true;

    public Account(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Generated(hash = 721714932)
    public Account(String id, String server, String username, String password,
            Integer priority, Boolean valid) {
        this.id = id;
        this.server = server;
        this.username = username;
        this.password = password;
        this.priority = priority;
        this.valid = valid;
    }

    @Generated(hash = 882125521)
    public Account() {
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServer() {
        return this.server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public Integer getPriority() {
        return this.priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Boolean getValid() {
        return this.valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }
}
