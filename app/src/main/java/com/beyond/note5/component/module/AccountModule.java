package com.beyond.note5.component.module;

import com.beyond.note5.presenter.AccountPresenter;
import com.beyond.note5.presenter.AccountPresenterImpl;
import com.beyond.note5.view.AccountView;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author: beyond
 * @date: 2019/8/27
 */

@Module
public class AccountModule {
    private AccountView accountView;

    public AccountModule(AccountView accountView) {
        this.accountView = accountView;
    }

    @Provides
    @Singleton
    AccountPresenter provideAccountPresenter() {
        return new AccountPresenterImpl(accountView);
    }
}
