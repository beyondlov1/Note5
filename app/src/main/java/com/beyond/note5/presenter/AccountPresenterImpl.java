package com.beyond.note5.presenter;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Account;
import com.beyond.note5.model.AccountModel;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.OkWebDavUtil;
import com.beyond.note5.view.AccountView;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.handler.OkHttpSardine2;

import java.util.List;

public class AccountPresenterImpl implements AccountPresenter {
    
    private AccountModel accountModel;

    private AccountView accountView;

    public AccountPresenterImpl(AccountModel accountModel, AccountView accountView) {
        this.accountModel = accountModel;
        this.accountView = accountView;
    }

    @Override
    public void login(Account account) {
        MyApplication.getInstance().getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Sardine sardine = new OkHttpSardine2();
                    sardine.setCredentials(account.getUsername(), account.getPassword());
                    if (!sardine.exists(account.getServer())){
                        OkWebDavUtil.mkRemoteDirQuietly(sardine,account.getServer());
                    }
                    boolean available = OkWebDavUtil.isAvailable(
                            account.getServer(),
                            OkWebDavUtil.concat(account.getServer(), MyApplication.LOGIN_PATH),
                            account.getUsername(),
                            account.getPassword());
                    if (available){
                        account.setId(IDUtil.uuid());
                        accountModel.add(account);
                        accountView.onLoginSuccess(account);
                    }else {
                        throw new RuntimeException("登录失败");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    accountView.onLoginFail(e);
                }
            }
        });
    }

    @Override
    public void findAll() {
        try {
            List<Account> all = accountModel.findAll();
            accountView.onFindAllSuccess(all);
        }catch (Exception e){
            e.printStackTrace();
            accountView.onFindAllFail(e);
        }
    }
}
