package com.beyond.note5.model;

import com.beyond.note5.bean.Account;

import java.util.List;

public interface AccountModel extends Model<Account> {
    List<Account> selectByServer(String server);
    List<Account> findAllValid();
}
