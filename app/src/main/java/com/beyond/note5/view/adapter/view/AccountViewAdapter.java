package com.beyond.note5.view.adapter.view;

import com.beyond.note5.bean.Account;
import com.beyond.note5.view.AccountView;

import java.util.List;

/**
 * @author: beyond
 * @date: 2019/7/15
 */

public class AccountViewAdapter implements AccountView {
    @Override
    public void onLoginSuccess(Account account) {

    }

    @Override
    public void onLoginFail(Exception e) {

    }

    @Override
    public void onFindAllSuccess(List<Account> all) {

    }

    @Override
    public void onFindAllFail(Exception e) {

    }

    @Override
    public void onUpdateSuccess(Account account) {

    }

    @Override
    public void onUpdateFail(Exception e) {

    }

    @Override
    public void onDeleteSuccess(Account account) {

    }

    @Override
    public void onDeleteFail(Account account) {

    }
}
