package com.beyond.note5.presenter;

import com.beyond.note5.bean.Account;

public interface AccountPresenter {
    void update(Account account);

    void login(Account account);

    void findAll();

    void delete(Account account);
}
