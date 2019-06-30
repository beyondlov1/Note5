package com.beyond.note5.view;

import com.beyond.note5.bean.Account;

import java.util.List;

public interface AccountView  {
    void onLoginSuccess(Account account);

    void onLoginFail(Exception e);

    void onFindAllSuccess(List<Account> all);

    void onFindAllFail(Exception e);
}
