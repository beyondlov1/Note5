package com.beyond.note5.bean;

import java.util.Date;

public class AccountWrapper {

    private Account account;

    private Date noteLastSyncTime;
    private Date todoLastSyncTime;

    public AccountWrapper(Account account) {
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Date getTodoLastSyncTime() {
        return todoLastSyncTime;
    }

    public void setTodoLastSyncTime(Date todoLastSyncTime) {
        this.todoLastSyncTime = todoLastSyncTime;
    }

    public Date getNoteLastSyncTime() {
        return noteLastSyncTime;
    }

    public void setNoteLastSyncTime(Date noteLastSyncTime) {
        this.noteLastSyncTime = noteLastSyncTime;
    }

    public String getPassword() {
        return account.getPassword();
    }

    public void setPassword(String password) {
        account.setPassword(password);
    }

    public String getUsername() {
        return account.getUsername();
    }

    public void setUsername(String username) {
        account.setUsername(username);
    }

    public String getId() {
        return account.getId();
    }

    public void setId(String id) {
        account.setId(id);
    }

    public String getServer() {
        return account.getServer();
    }

    public void setServer(String server) {
        account.setServer(server);
    }

    public Integer getPriority() {
        return account.getPriority();
    }

    public void setPriority(Integer priority) {
        account.setPriority(priority);
    }

    public Boolean getValid() {
        return account.getValid();
    }

    public void setValid(Boolean valid) {
        account.setValid(valid);
    }
}
