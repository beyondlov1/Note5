package com.beyond.note5.presenter;

import com.beyond.note5.bean.Account;
import com.beyond.note5.bean.AccountWrapper;

public interface AccountPresenter {
    void update(Account account);

    void login(Account account);

    void findAll();

    void delete(Account account);

    AccountWrapper wrap(Account account);
}
